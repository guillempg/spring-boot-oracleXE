package com.example.springjpaoracle;

import com.example.springjpaoracle.client.ApplicationClient;
import com.example.springjpaoracle.client.KeycloakUserCache;
import com.example.springjpaoracle.dto.*;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.parameter.*;
import com.example.springjpaoracle.repository.CourseRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
public class CucumberSteps
{
    public static final String REGISTER_STUDENT_OUTPUT = "register-student-output";
    public static final ParameterizedTypeReference<List<StudentCourseScoreResponse>> STUDENT_COURSE_SCORE_LIST = new ParameterizedTypeReference<>()
    {
    };
    public static final ParameterizedTypeReference<List<LightweightStudentResponse>> LIGHT_STUDENT_RESPONSE = new ParameterizedTypeReference<>()
    {
    };
    private final ConfigurableApplicationContext context;
    private final CompositeRepository uberRepository;
    private final StreamBridge streamBridge;
    private final MessageChannel studentDeleteChannel;
    private final RabbitMQSupport rabbitMQSupport;
    private final ApplicationClient applicationClient;
    private final KeycloakUserCache keycloakUserCache;
    private final CourseRepository courseRepository;
    private final SessionFactory sessionFactory;

    public CucumberSteps(final ConfigurableApplicationContext context,
                         final CompositeRepository uberRepository,
                         final StreamBridge streamBridge,
                         @Qualifier("studentDeleteInput-in-0") final MessageChannel studentDeleteChannel,
                         final RabbitMQSupport rabbitMQSupport,
                         final ApplicationClient applicationClient,
                         final KeycloakUserCache keycloakUserCache,
                         final CourseRepository courseRepository,
                         final SessionFactory sessionFactory)
    {
        this.context = context;
        this.applicationClient = applicationClient;
        this.uberRepository = uberRepository;
        this.streamBridge = streamBridge;
        this.studentDeleteChannel = studentDeleteChannel;
        this.rabbitMQSupport = rabbitMQSupport;
        this.keycloakUserCache = keycloakUserCache;
        this.courseRepository = courseRepository;
        this.sessionFactory = sessionFactory;
    }

    @After
    @Before
    public void cleanTables()
    {
        uberRepository.cleanTables();
        rabbitMQSupport.reset();
    }

    @Given("{string} user {string} creates courses:")
    public void createCourses(String role, String adminUser, List<CourseHttpRequest> courseRequests)
    {
        courseRequests.forEach(req ->
                applicationClient.getWebTestClient().post()
                        .uri("/courses/")
                        .attributes(getOauth2Client(adminUser))
                        .bodyValue(req)
                        .exchange()
                        .expectStatus().isCreated());
    }

    @When("{string} user {string} requests list of courses")
    public void userRequestsCourseList(String role, String adminUser)
    {
        applicationClient.getWebTestClient().get()
                .uri("/courses")
                .attributes(getOauth2Client(adminUser))
                .exchange()
                .expectStatus().isOk();
    }

    @Then("cached courses are:")
    public void cachedCourses(List<String> courseNames)
    {
        final List<Course> courses = courseRepository.findByNameIgnoreCaseIn(courseNames);

        courses.forEach(c -> assertTrue(sessionFactory.getCache().contains(Course.class, c.getId())));

        //possibly overkill but just showing that the 2nd level cache is being hit (when we query by id)
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        courses.forEach(c -> courseRepository.findById(c.getId()));
        assertThat(statistics.getSecondLevelCacheHitCount()).isEqualTo(3L);
        statistics.clear();
    }

    @Given("user {string} retrieves external ids for users:")
    public void retrieves_external_ids_for_users(String adminUser, List<String> usernames)
    {
        usernames.forEach(username -> keycloakUserCache.getKeycloakIdByUsernameAsAdmin(adminUser, username));
    }

    @When("{string} user {string} logs into the application with password {string}")
    public void userLogsIn(String role, String username, String password)
    {
        final AbstractAuthenticationToken token = applicationClient.getAccessToken(username, password);
        List<String> roles = token.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        assertThat(roles).contains(role);
    }

    @Given("the app is running")
    public void applicationIsRunning()
    {
        assertTrue(context.isRunning());
    }

    @When("admin user {string} successfully register student with details:")
    public void registerStudents(final String adminUsername,
                                 List<StudentHttpRequest> registrationDetails)
    {
        registrationDetails.forEach(registrationDetail ->
                registerStudent(adminUsername, new KeycloakUser().setId(registrationDetail.getStudentKeycloakId()), registrationDetail.getCourseNames()));
    }

    @When("{string} successfully register student with username {keycloakUser} on courses {courses}")
    public void registerStudent(final String adminUser, KeycloakUser keycloakUser, List<String> coursesRequested)
    {
        final String url = "/students/register";
        List<PhoneHttpRequest> phonesSubmitted = Collections.emptyList();

        final StudentHttpRequest studentHttpRequest = new StudentHttpRequest()
                .setStudentKeycloakId(keycloakUser.getId())
                .setCourseNames(coursesRequested)
                .setPhones(phonesSubmitted);

        StudentResponse expected = new StudentResponse().setKeycloakId(keycloakUser.getId())
                .setCourses(coursesRequested.stream().map(CourseResponse::new).collect(Collectors.toList()));

        applicationClient.getWebTestClient().post().uri(url)
                .attributes(getOauth2Client(adminUser))
                .bodyValue(studentHttpRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StudentResponse.class).isEqualTo(expected);
    }

    @When("{string} submits a request to delete student {keycloakUser}")
    public void deleteStudent(
            final String adminUser,
            final KeycloakUser studentUser)
    {
        applicationClient.getWebTestClient().delete().uri("/students/{keycloakId}", studentUser.getId())
                .attributes(getOauth2Client(adminUser))
                .exchange()
                .expectStatus().isOk();
    }

    @Then("{string} verifies student {keycloakUser} and her courses registrations are deleted")
    public void checkStudentAndRegistrationsDeleted(
            final String adminUsername,
            final KeycloakUser studentUser)
    {
        applicationClient.getWebTestClient().get().uri("/students/{keycloakId}", studentUser.getId())
                .attributes(getOauth2Client(adminUsername))
                .exchange()
                .expectStatus().isNotFound();
    }

    @When("{string} request the list of students enrolled to course {string}:")
    public void requestListOfEnrolledStudents(
            final String adminUsername,
            final String courseName,
            final List<KeycloakUser> expectedStudentNames)
    {
        final List<LightweightStudentResponse> students = applicationClient.getWebTestClient()
                .get().uri("/students/listEnrolledStudents?courseName={name}", courseName)
                .attributes(getOauth2Client(adminUsername))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LIGHT_STUDENT_RESPONSE)
                .returnResult().getResponseBody();

        List<String> studentNames = Objects.requireNonNull(students).stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .collect(Collectors.toList());
        assertThat(studentNames)
                .containsExactlyInAnyOrder(expectedStudentNames.stream().map(KeycloakUser::getId).toArray(String[]::new));
    }

    @When("we registers students via messaging with details:")
    public void weRegisterStudentsViaMessagingWithDetails(final List<StudentHttpRequest> registeredUsernames)
    {
        registeredUsernames.forEach(request -> streamBridge.send(
                REGISTER_STUDENT_OUTPUT,
                MessageBuilder.withPayload(request).setHeader("myHeader", "myValue").build()));
    }

    @Then("{string} verifies that students exist with the following names:")
    public void studentsShouldExitsWithFollowingNames(
            final String adminUsername,
            final List<KeycloakUser> expectedNames)
    {
        List<String> expectedKeycloakIds = expectedNames.stream()
                .map(KeycloakUser::getId)
                .collect(Collectors.toList());

        await().until(() -> expectedKeycloakIds.stream()
                .map(keycloakId -> applicationClient.getWebTestClient()
                        .get().uri("/students/{keycloakId}", keycloakId)
                        .attributes(getOauth2Client(adminUsername))
                        .exchange().expectBody(Student.class)
                        .returnResult())
                .map(EntityExchangeResult::getStatus)
                .allMatch(HttpStatus::is2xxSuccessful));
    }

    @When("student with name {keycloakUser} is deleted via messaging")
    public void deleteStudentWithSsnViaMessaging(final KeycloakUser studentUser)
    {
        studentDeleteChannel.send(MessageBuilder.withPayload(studentUser.getId()).build());
    }

    @When("{string} lists students not registered to course {string}:")
    public void listStudentsNotRegisteredToCourse(
            final String adminUsername,
            final String courseName,
            final List<KeycloakUser> expectedStudentNames)
    {
        final List<LightweightStudentResponse> studentsResponse = applicationClient.getWebTestClient()
                .get().uri("/students/listStudentsNotEnrolled?courseName={name}", courseName)
                .attributes(getOauth2Client(adminUsername))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LIGHT_STUDENT_RESPONSE)
                .returnResult().getResponseBody();

        List<String> studentNames = studentsResponse.stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .collect(Collectors.toList());
        assertThat(studentNames)
                .containsExactlyInAnyOrder(expectedStudentNames.stream().map(KeycloakUser::getId).toArray(String[]::new));
    }

    @Then("teacher {keycloakUser} sees student scores for course {string}:")
    public void viewStudentScores(KeycloakUser teacherUser, String courseName, List<RegisterScore> registerScores)
    {
        final List<StudentCourseScoreResponse> studentsResponse = applicationClient.getWebTestClient()
                .get().uri("/courses/score/{name}", courseName)
                .attributes(getOauth2Client(teacherUser.getUsername()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(STUDENT_COURSE_SCORE_LIST)
                .returnResult().getResponseBody();

        final StudentCourseScoreResponse[] expected = registerScores.stream()
                .map(score -> new StudentCourseScoreResponse()
                        .setScore(score.getScore())
                        .setStudentKeycloakId(score.getKeycloakId())
                        .setTeacherKeycloakId(teacherUser.getId())
                        .setCourseName(courseName))
                .toArray(StudentCourseScoreResponse[]::new);

        assertThat(studentsResponse).containsExactlyInAnyOrder(expected);
    }

    @Then("student {keycloakUser} sees student scores:")
    public void student_sees_student_scores_with_hack(KeycloakUser studentUser, List<RegisterScore> registerScores)
    {
        final List<StudentCourseScoreResponse> studentsResponse = applicationClient.getWebTestClient()
                .get().uri("/courses/score/student/{studentKeyCloakId}", studentUser.getId())
                .attributes(getOauth2Client(studentUser.getUsername()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(STUDENT_COURSE_SCORE_LIST)
                .returnResult().getResponseBody();

        final StudentCourseScoreResponse[] expected = registerScores.stream()
                .map(score -> new StudentCourseScoreResponse()
                        .setScore(score.getScore())
                        .setStudentKeycloakId(studentUser.getId())
                        .setCourseName(score.getCourseName()))
                .toArray(StudentCourseScoreResponse[]::new);

        assertThat(studentsResponse)
                .usingElementComparatorIgnoringFields("teacherKeycloakId")
                .containsExactlyInAnyOrder(expected);
    }

    @When("{string} saves teacher {keycloakUser}")
    public void saveTeacher(final String adminUsername,
                            final KeycloakUser teacherUser)
    {
        SaveTeacherRequest req = new SaveTeacherRequest();
        req.setKeycloakId(teacherUser.getId());

        applicationClient.getWebTestClient().post().uri("/courses/saveteacher")
                .attributes(getOauth2Client(adminUsername))
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.keycloakId").isEqualTo(teacherUser.getId());

    }

    @When("{string} assigns teacher with details:")
    public void assignsTeacher(final String adminUsername,
                               final List<TeacherDetail> teacherDetails)
    {
        teacherDetails.stream().flatMap(teacherDetail ->
                        teacherDetail.getCourseNames().stream()
                                .map(courseName ->
                                        new AssignTeacherRequest().setCourseName(courseName)
                                                .setTeacherKeycloakId(teacherDetail.getKeycloakId())))
                .forEach(req ->
                        applicationClient.getWebTestClient().post().uri("/courses/assignteacher")
                                .attributes(getOauth2Client(adminUsername))
                                .bodyValue(req)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.courseName").isEqualTo(req.getCourseName())
                                .jsonPath("$.teacherKeycloakId").isEqualTo(req.getTeacherKeycloakId()));
    }

    @When("{keycloakUser} registers student scores:")
    public void registers_student_scores(final KeycloakUser teacherUser,
                                         final List<RegisterScore> registerScores)
    {
        registerScores.stream()
                .map(registerScore -> new ScoreRequest()
                        .setStudentKeycloakId(registerScore.getKeycloakId())
                        .setScore(registerScore.getScore())
                        .setCourseName(registerScore.getCourseName())
                        .setTeacherKeycloakId(teacherUser.getId()))
                .forEach(r ->
                        applicationClient.getWebTestClient().post().uri("/courses/score")
                                .attributes(getOauth2Client(teacherUser.getUsername()))
                                .bodyValue(r)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.courseName").isEqualTo(r.getCourseName())
                                .jsonPath("$.studentKeycloakId").isEqualTo(r.getStudentKeycloakId())
                                .jsonPath("$.teacherKeycloakId").isEqualTo(r.getTeacherKeycloakId())
                                .jsonPath("$.score").isEqualTo(r.getScore()));
    }

    private Consumer<Map<String, Object>> getOauth2Client(final String username)
    {
        return oauth2AuthorizedClient(applicationClient.getAuthorizedClient(username));
    }
}
