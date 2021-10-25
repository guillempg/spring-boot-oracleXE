package com.example.springjpaoracle;

import com.example.springjpaoracle.dto.CourseResponse;
import com.example.springjpaoracle.dto.LightweightStudentResponse;
import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.model.Student;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
        final List<CourseHttpRequest> coursesRequested = courseNames.stream()
                .map(CourseHttpRequest::new)
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

    private List<StudentHttpRequest> tableToStudentsRequest(
            final String adminUsername,
            final DataTable dataTable)
    {
        return dataTable.asMaps().stream().map(c -> rowToRequest(adminUsername, c)).collect(Collectors.toList());
    }

    private StudentHttpRequest rowToRequest(
            final String adminUsername,
            final Map<String, String> row)
    {
        List<CourseHttpRequest> courses = Arrays.stream(row.get("courses").split(",")).map(String::trim)
                .map(CourseHttpRequest::new).collect(Collectors.toList());
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

    static class StudentHttpRequest
    {
        final String keycloakId;
        final List<CourseHttpRequest> courses;
        final List<PhoneHttpRequest> phones;

        public StudentHttpRequest(final String keycloakId,
                                  final List<CourseHttpRequest> courses,
                                  final List<PhoneHttpRequest> phones)
        {
            this.keycloakId = keycloakId;
            this.courses = courses;
            this.phones = phones;
        }

        public List<CourseHttpRequest> getCourses()
        {
            return courses;
        }

        public String getKeycloakId()
        {
            return keycloakId;
        }

        public List<PhoneHttpRequest> getPhones()
        {
            return phones;
        }
    }

    static class CourseHttpRequest
    {
        final String name;

        public CourseHttpRequest(final String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
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
