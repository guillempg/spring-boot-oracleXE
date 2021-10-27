package com.example.springjpaoracle;

import com.example.springjpaoracle.dto.*;
import com.example.springjpaoracle.model.Student;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class CucumberSteps
{
    public static final String REGISTER_STUDENT_OUTPUT = "register-student-output";
    private final ConfigurableApplicationContext context;
    private final TestRestTemplate applicationTemplate;
    private final KeycloakClient keycloakClient;
    private final CompositeRepository uberRepository;
    private final StreamBridge streamBridge;
    private final MessageChannel studentDeleteChannel;
    private final RabbitMQSupport rabbitMQSupport;
    private final ThreadLocal<TestCache> testCache;

    public CucumberSteps(final ConfigurableApplicationContext context,
                         final TestRestTemplate applicationTemplate,
                         final KeycloakClient keycloakClient,
                         final CompositeRepository uberRepository,
                         final StreamBridge streamBridge,
                         @Qualifier("studentDeleteInput-in-0") final MessageChannel studentDeleteChannel,
                         final RabbitMQSupport rabbitMQSupport)
    {
        this.context = context;
        this.applicationTemplate = applicationTemplate;
        this.testCache = new ThreadLocal<>();
        this.testCache.set(new TestCache(keycloakClient));
        this.keycloakClient = keycloakClient;
        this.uberRepository = uberRepository;
        this.streamBridge = streamBridge;
        this.studentDeleteChannel = studentDeleteChannel;
        this.rabbitMQSupport = rabbitMQSupport;
    }

    @After
    @Before
    public void cleanTables()
    {
        uberRepository.cleanTables();
        rabbitMQSupport.reset();
    }

    @When("user {string} logs into the application with password {string}")
    public void userLogsIn(String username, String password)
    {
        testCache.get().getToken(username, password);
    }

    @When("{string} user {string} logs into the application with password {string}")
    public void userLogsIn(String role, String username, String password)
    {
        final String token = keycloakClient.getAccessToken(username, password);
        // TODO finish this
        //assertThat(decodeRolesFromToken(token)).contains(role);
        testCache.get().getToken(username, password);
    }

    @Given("the app is running")
    public void applicationIsRunning()
    {
        System.out.println("active profile:" + Arrays.stream(context.getEnvironment().getActiveProfiles()).collect(Collectors.joining(",")));
        assertTrue(context.isRunning());
    }

    @When("admin user {string} successfully register student with details:")
    public void registerStudents(final String username,
                                 io.cucumber.datatable.DataTable dataTable)
    {
        for (Map<String, String> row : dataTable.asMaps())
        {
            try
            {
                registerStudent(username, row.get("name"), row.get("courses"));
            } catch (Exception e)
            {
                e.printStackTrace();
                fail("Student not registered: " + e.getMessage());
            }
        }
    }

    @When("{string} successfully register student with username {string} on courses {string}")
    public void registerStudent(final String adminUser,
                                final String studentUsername,
                                final String courses)
    {
        final String url = "/students/register";

        final List<String> courseNames = Arrays.stream(courses.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        final List<String> coursesRequested = courseNames.stream()
                .collect(Collectors.toList());

        List<PhoneHttpRequest> phonesSubmitted = Collections.emptyList();

        String keycloakId = testCache.get().getKeycloakIdByUsername(adminUser, studentUsername);
        final StudentHttpRequest s = new StudentHttpRequest(
                keycloakId,
                coursesRequested,
                phonesSubmitted);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUser));
        HttpEntity<StudentHttpRequest> req = new HttpEntity<>(s, headers);

        final ResponseEntity<StudentResponse> responseEntity = applicationTemplate.postForEntity(url, req, StudentResponse.class);

        final StudentResponse registeredStudent = responseEntity.getBody();
        assertEquals(registeredStudent.getKeycloakId(), keycloakId);

        final List<String> registeredCourseNames = registeredStudent.getCourses().stream()
                .map(CourseResponse::getName)
                .collect(Collectors.toList());

        assertThat(registeredCourseNames)
                .containsExactlyInAnyOrder(courseNames.toArray(new String[0]));
    }

    @When("{string} submits a request to delete student {string}")
    public void deleteStudent(
            final String adminUser,
            final String studentUser)
    {
        final String keycloakId = testCache.get().getKeycloakIdByUsername(adminUser, studentUser);
        final String url = "/students/{keycloakId}";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUser));
        HttpEntity<Void> req = new HttpEntity<>(null, headers);

        final ResponseEntity<Void> outcome = applicationTemplate.exchange(url, HttpMethod.DELETE, req, Void.class, keycloakId);
        assertThat(outcome.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Then("{string} verifies student {string} and her courses registrations are deleted")
    public void checkStudentAndRegistrationsDeleted(
            final String adminUsername,
            final String studentUsername)
    {
        final String keycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, studentUsername);
        final String url = "/students/{keycloakId}";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUsername));
        HttpEntity<Student> req = new HttpEntity<>(null, headers);

        final ResponseEntity<Student> student = applicationTemplate.exchange(url, HttpMethod.GET, req, Student.class, keycloakId);
        assertEquals(student.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @When("{string} request the list of students enrolled to course {string}:")
    public void requestListOfEnrolledStudents(
            final String adminUsername,
            final String courseName,
            final DataTable dataTable)
    {
        final String url = "/students/listEnrolledStudents?courseName={name}";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUsername));
        HttpEntity<Student> req = new HttpEntity<>(null, headers);
        final ResponseEntity<List<LightweightStudentResponse>> student = applicationTemplate.exchange(url, HttpMethod.GET, req,
                new ParameterizedTypeReference<>()
                {
                }, courseName);
        final List<String> expectedStudentNames = dataTable.asMaps().stream()
                .map(entry -> entry.get("name"))
                .collect(Collectors.toList());
        List<String> studentNames = student.getBody().stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .map(keycloakId -> testCache.get().findUsernameByKeycloakId(keycloakId, adminUsername))
                .collect(Collectors.toList());
        assertThat(studentNames)
                .containsExactlyInAnyOrder(expectedStudentNames.toArray(String[]::new));
    }

    @When("{string} registers students via messaging with details:")
    public void weRegisterStudentsViaMessagingWithDetails(
            final String adminUsername,
            final DataTable registeredUsernames)
    {
        tableToStudentsRequest(adminUsername, registeredUsernames).forEach(request -> streamBridge.send(
                REGISTER_STUDENT_OUTPUT,
                MessageBuilder.withPayload(request).setHeader("myHeader", "myValue").build()));
    }

    @Then("{string} verifies that students exist with the following names:")
    public void studentsShouldExitsWithFollowingNames(
            final String adminUsername,
            final DataTable expectedNames)
    {
        List<String> expectedKeycloakIds = expectedNames.asList()
                .stream()
                .skip(1)
                .map(name -> testCache.get().getKeycloakIdByUsername(adminUsername, name))
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUsername));
        HttpEntity<Student> req = new HttpEntity<>(null, headers);
        final String url = "/students/{keycloakId}";

        await().until(() -> expectedKeycloakIds.stream()
                .map(keycloakId -> applicationTemplate.exchange(url, HttpMethod.GET, req, Student.class, keycloakId))
                .map(ResponseEntity::getStatusCode)
                .allMatch(HttpStatus::is2xxSuccessful));
    }

    @When("{string} deletes student with name {string} via messaging")
    public void deleteStudentWithSsnViaMessaging(
            final String adminUsername,
            final String studentName)
    {
        final String keycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, studentName);
        studentDeleteChannel.send(MessageBuilder.withPayload(keycloakId).build());
    }

    @When("{string} lists students not registered to course {string}:")
    public void listStudentsNotRegisteredToCourse(
            final String adminUsername,
            final String courseName,
            final DataTable dataTable)
    {
        final String url = "/students/listStudentsNotEnrolled?courseName={name}";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUsername));
        HttpEntity<Student> req = new HttpEntity<>(null, headers);

        final ResponseEntity<List<LightweightStudentResponse>> student = applicationTemplate.exchange(url, HttpMethod.GET, req,
                new ParameterizedTypeReference<>()
                {
                }, courseName);
        final List<String> expectedStudentNames = dataTable.asMaps().stream()
                .map(entry -> entry.get("name"))
                .collect(Collectors.toList());
        List<String> studentNames = student.getBody().stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .map(keycloakId -> testCache.get().findUsernameByKeycloakId(keycloakId, adminUsername))
                .collect(Collectors.toList());
        assertThat(studentNames)
                .containsExactlyInAnyOrderElementsOf(expectedStudentNames);
    }

    @Then("teacher {string} sees student scores for course {string} with hack {string}:")
    public void viewStudentScores(String teacherUsername, String courseName, String adminUsername, DataTable dataTable)
    {
        final String teacherKeycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, teacherUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(teacherUsername));

        HttpEntity<SaveTeacherRequest> request = new HttpEntity<>(null, headers);
        final String url = "/courses/score/" + courseName;
        final ResponseEntity<List<StudentCourseScoreResponse>> responseEntity = applicationTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>()
                {
                });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        final StudentCourseScoreResponse[] expected = dataTable.asMaps().stream()
                .map(map ->
                {
                    final String studentName = map.get("studentName");
                    final String studentKeycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, studentName);

                    return new StudentCourseScoreResponse()
                            .setScore(Double.valueOf(map.get("score")))
                            .setStudentKeycloakId(studentKeycloakId)
                            .setTeacherKeycloakId(teacherKeycloakId)
                            .setCourseName(courseName);
                })
                .toArray(StudentCourseScoreResponse[]::new);

        assertThat(responseEntity.getBody()).containsExactlyInAnyOrder(expected);
    }

    @Then("student {string} sees student scores with hack {string}:")
    public void student_sees_student_scores_with_hack(String studentUsername, String adminUsername, io.cucumber.datatable.DataTable dataTable)
    {
        final String studentKeycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, studentUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(studentUsername));

        HttpEntity<SaveTeacherRequest> request = new HttpEntity<>(null, headers);
        final String url = "/courses/score/student/" + studentKeycloakId;
        final ResponseEntity<List<StudentCourseScoreResponse>> responseEntity = applicationTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>()
                {
                });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        final StudentCourseScoreResponse[] expected = dataTable.asMaps().stream()
                .map(map ->
                {
                    final String courseName = map.get("courseName");

                    return new StudentCourseScoreResponse()
                            .setScore(Double.valueOf(map.get("score")))
                            .setStudentKeycloakId(studentKeycloakId)
                            .setTeacherKeycloakId(studentKeycloakId)
                            .setCourseName(courseName);
                })
                .toArray(StudentCourseScoreResponse[]::new);

        assertThat(responseEntity.getBody())
                .usingElementComparatorIgnoringFields("teacherKeycloakId")
                .containsExactlyInAnyOrder(expected);
    }

    @When("{string} saves teacher {string}")
    public void saveTeacher(final String adminUsername,
                            final String teacherName)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(adminUsername));

        final String teacherKeycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, teacherName);

        SaveTeacherRequest req = new SaveTeacherRequest();
        req.setKeycloakId(teacherKeycloakId);

        HttpEntity<SaveTeacherRequest> request = new HttpEntity<>(req, headers);
        final String url = "/courses/saveteacher";
        final ResponseEntity<TeacherResponse> responseEntity = applicationTemplate.postForEntity(url, request, TeacherResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getKeycloakId()).isEqualTo(teacherKeycloakId);
    }

    @When("{string} assigns teacher with details:")
    public void assignsTeacher(final String adminUsername,
                               final DataTable dataTable)
    {
        final String url = "/courses/assignteacher";

        tableToAssignTeacherRequest(adminUsername, dataTable).forEach(req ->
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(testCache.get().getToken(adminUsername));

            HttpEntity<AssignTeacherRequest> request = new HttpEntity<>(req, headers);

            final ResponseEntity<TeacherAssignationResponse> responseEntity = applicationTemplate.postForEntity(url, request, TeacherAssignationResponse.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody().getCourseName()).isEqualTo(req.getCourseName());
            assertThat(responseEntity.getBody().getTeacherKeycloakId()).isEqualTo(req.getTeacherKeycloakId());
        });
    }

    @When("{string} registers student scores with hack {string}:")
    public void registers_student_scores(final String teacherUsername,
                                         final String adminUsername,
                                         final DataTable dataTable)
    {
        final String url = "/courses/score";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(testCache.get().getToken(teacherUsername));

        tableToScoreRequest(teacherUsername, adminUsername, dataTable).forEach(r ->
        {
            final HttpEntity<ScoreRequest> scoreRequest = new HttpEntity<>(r, headers);
            final ResponseEntity<StudentCourseScoreResponse> response = applicationTemplate.postForEntity(url, scoreRequest,
                    StudentCourseScoreResponse.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getCourseName()).isEqualTo(r.getCourseName());
            assertThat(response.getBody().getScore()).isEqualTo(r.getScore());
            assertThat(response.getBody().getStudentKeycloakId()).isEqualTo(r.getStudentKeycloakId());
            assertThat(response.getBody().getTeacherKeycloakId()).isEqualTo(r.getTeacherKeycloakId());
        });
    }

    private List<AssignTeacherRequest> tableToAssignTeacherRequest(final String adminUsername,
                                                                   final DataTable dataTable)
    {
        return dataTable.asMaps().stream()
                .flatMap(e -> rowToTeacherAssignmentRequest(adminUsername, e))
                .collect(Collectors.toList());
    }

    private Stream<AssignTeacherRequest> rowToTeacherAssignmentRequest(final String adminUsername,
                                                                       final Map<String, String> row)
    {
        final String teacherName = row.get("name");
        final String teacherKeycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, teacherName);

        List<String> courses = Arrays.stream(row.get("coursesAssigned").split(",")).map(String::trim)
                .collect(Collectors.toList());

        return courses.stream()
                .map(courseName -> new AssignTeacherRequest()
                        .setCourseName(courseName)
                        .setTeacherKeycloakId(teacherKeycloakId))
                .collect(Collectors.toList()).stream();
    }

    private List<ScoreRequest> tableToScoreRequest(
            final String teacherUsername,
            final String adminUsername,
            final DataTable dataTable)
    {
        return dataTable.asMaps().stream()
                .map(e -> rowToScoreRequest(adminUsername, teacherUsername, e))
                .collect(Collectors.toList());
    }

    private ScoreRequest rowToScoreRequest(final String adminUsername,
                                           final String teacherUsername,
                                           final Map<String, String> row)
    {
        final String studentName = row.get("studentName");
        final String courseName = row.get("courseName");
        final Double score = Double.valueOf(row.get("score"));

        final ScoreRequest req = new ScoreRequest();
        req.setScore(score);
        req.setCourseName(courseName);
        req.setStudentKeycloakId(testCache.get().getKeycloakIdByUsername(adminUsername, studentName));
        req.setTeacherKeycloakId(testCache.get().getKeycloakIdByUsername(adminUsername, teacherUsername));
        return req;
    }

    private List<StudentHttpRequest> tableToStudentsRequest(
            final String adminUsername,
            final DataTable dataTable)
    {
        return dataTable.asMaps().stream().map(c -> rowToStudentRequest(adminUsername, c)).collect(Collectors.toList());
    }

    private StudentHttpRequest rowToStudentRequest(
            final String adminUsername,
            final Map<String, String> row)
    {
        List<String> courses = Arrays.stream(row.get("courses").split(",")).map(String::trim)
                .collect(Collectors.toList());
        List<PhoneHttpRequest> phonesSubmitted = Collections.emptyList();
        if (row.get("phones") != null &&
                row.get("phones").length() > 0)
        {
            phonesSubmitted = Arrays.stream(row.get("phones").split(","))
                    .map(PhoneHttpRequest::new)
                    .collect(Collectors.toList());
        }

        final String keycloakId = testCache.get().getKeycloakIdByUsername(adminUsername, row.get("name"));
        return new StudentHttpRequest(keycloakId, courses, phonesSubmitted);
    }

    @Value
    static class StudentHttpRequest
    {
        final String studentKeycloakId;
        final List<String> courseNames;
        final List<PhoneHttpRequest> phones;
    }

    static class PhoneHttpRequest
    {
        final String phoneNumber;


        public PhoneHttpRequest(final String phoneNumber)
        {
            this.phoneNumber = phoneNumber;
        }

        public String getPhoneNumber()
        {
            return phoneNumber;
        }
    }
}
