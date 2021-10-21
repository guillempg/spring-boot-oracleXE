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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final TestRestTemplate template;
    private final CompositeRepository uberRepository;
    private final StreamBridge streamBridge;
    private final MessageChannel studentDeleteChannel;
    private final RabbitMQSupport rabbitMQSupport;

    public CucumberSteps(final ConfigurableApplicationContext context,
                         final TestRestTemplate template,
                         final CompositeRepository uberRepository,
                         final StreamBridge streamBridge,
                         @Qualifier("studentDeleteInput-in-0") final MessageChannel studentDeleteChannel,
                         final RabbitMQSupport rabbitMQSupport)
    {
        this.context = context;
        this.template = template;
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

    @Given("the app is running")
    public void applicationIsRunning()
    {
        assertTrue(context.isRunning());
    }

    @When("we successfully register student with details:")
    public void registerStudents(io.cucumber.datatable.DataTable dataTable)
    {
        for (Map<String, String> row : dataTable.asMaps())
        {
            try
            {
                registerStudent(row.get("keycloakId"), row.get("phones"), row.get("courses"));
            } catch (Exception e)
            {
                fail("Student not registered: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @When("we successfully register student with name {string} and keycloakId {string} and phones {string} on courses {string}")
    public void registerStudent(final String keycloakId,
                                final String phoneNumbers,
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
        if (phoneNumbers != null)
        {
            phonesSubmitted = Arrays.stream(phoneNumbers.split(","))
                    .map(PhoneHttpRequest::new)
                    .collect(Collectors.toList());
        }
        final StudentHttpRequest s = new StudentHttpRequest(
                keycloakId,
                coursesRequested,
                phonesSubmitted);

        final ResponseEntity<StudentResponse> responseEntity = template.postForEntity(url, s, StudentResponse.class);
        final StudentResponse registeredStudent = responseEntity.getBody();
        assertEquals(registeredStudent.getKeycloakId(), keycloakId);

        final List<String> registeredCourseNames = registeredStudent.getCourses().stream()
                .map(CourseResponse::getName)
                .collect(Collectors.toList());

        assertThat(registeredCourseNames)
                .containsExactlyInAnyOrder(courseNames.toArray(new String[0]));
    }

    @When("we submit a request to delete student with keycloakId {string}")
    public void deleteStudent(final String socialSecurityNumber)
    {
        final String url = "/students/{ssn}";
        final ResponseEntity<Void> outcome = template.exchange(url, HttpMethod.DELETE, null, Void.class, socialSecurityNumber);
        assertTrue(outcome.getStatusCode().is2xxSuccessful());
    }

    @Then("the student with keycloakId {string} and her courses registrations are deleted")
    public void checkStudentAndRegistrationsDeleted(final String socialSecurityNumber)
    {
        final String url = "/students/{ssn}";
        final ResponseEntity<Student> student = template.getForEntity(url, Student.class, socialSecurityNumber);
        assertEquals(student.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @When("we request the list of students enrolled to course {string} we receive:")
    public void requestListOfEnrolledStudents(final String courseName, final DataTable dataTable)
    {
        final String url = "/students/listEnrolledStudents?courseName={name}";
        final ResponseEntity<List<LightweightStudentResponse>> student = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>()
                {
                }, courseName);
        final List<String> expectedStudentKeycloakIds = dataTable.asMaps().stream()
                .map(entry -> entry.get("keycloakId"))
                .collect(Collectors.toList());
        List<String> studentNames = student.getBody().stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .collect(Collectors.toList());
        assertThat(studentNames)
                .containsExactly(expectedStudentKeycloakIds.toArray(String[]::new));
    }

    @When("we register students via messaging with details:")
    public void weRegisterStudentsViaMessagingWithDetails(final DataTable dataTable)
    {
        tableToStudentsRequest(dataTable).forEach(request -> streamBridge.send(
                REGISTER_STUDENT_OUTPUT,
                MessageBuilder.withPayload(request).setHeader("myHeader", "myValue").build()));
    }

    @Then("students should exits with following security social numbers:")
    public void studentsShouldExitsWithFollowingSecuritySocialNumbers(DataTable socialSecurityNumbers)
    {
        await().until(() -> socialSecurityNumbers.asList()
                .stream().skip(1).map(keycloakId -> template.getForEntity("/students/{keycloakId}", Student.class, keycloakId))
                .map(ResponseEntity::getStatusCode).allMatch(HttpStatus::is2xxSuccessful));

    }

    @When("we receive a delete student with keycloakId {string} message")
    public void deleteStudentWithSsnViaMessaging(final String socialSecurityNumber)
    {
        studentDeleteChannel.send(MessageBuilder.withPayload(socialSecurityNumber).build());
    }

    @When("we list students not registered to course {string} we get:")
    public void listStudentsNotRegisteredToCourse(final String courseName, DataTable dataTable)
    {
        final String url = "/students/listStudentsNotEnrolled?courseName={name}";
        final ResponseEntity<List<LightweightStudentResponse>> student = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>()
                {
                }, courseName);
        final List<String> expectedStudentNames = dataTable.asMaps().stream()
                .map(entry -> entry.get("studentName"))
                .collect(Collectors.toList());
        List<String> studentKeycloakIds = student.getBody().stream()
                .map(LightweightStudentResponse::getKeycloakId)
                .collect(Collectors.toList());
        assertThat(studentKeycloakIds)
                .containsExactlyInAnyOrderElementsOf(expectedStudentNames);
    }

    private List<StudentHttpRequest> tableToStudentsRequest(DataTable dataTable)
    {
        return dataTable.asMaps().stream().map(this::rowToRequest).collect(Collectors.toList());
    }

    private StudentHttpRequest rowToRequest(Map<String, String> row)
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
        return new StudentHttpRequest(row.get("keycloakId"), courses, phonesSubmitted);
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
