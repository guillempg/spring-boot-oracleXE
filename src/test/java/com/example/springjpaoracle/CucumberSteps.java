package com.example.springjpaoracle;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.springjpaoracle.controller.StudentRepository;
import com.example.springjpaoracle.dto.LightweightStudentResponse;
import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.model.Student;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CucumberSteps
{
    private final ConfigurableApplicationContext context;
    private final TestRestTemplate template;
    private final CompositeRepository uberRepository;
    private final StudentRepository studentRepository;

    public CucumberSteps(final ConfigurableApplicationContext context, final TestRestTemplate template,
                         final CompositeRepository uberRepository, final StudentRepository studentRepository)
    {
        this.context = context;
        this.template = template;
        this.uberRepository = uberRepository;
        this.studentRepository = studentRepository;
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
                registerStudent(row.get("name"), row.get("ssn"), row.get("courses"));
            }
            catch (Exception e)
            {
                fail("Student not registered: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @After
    public void cleanTables()
    {
        uberRepository.cleanTables();
    }

    @When("we successfully register student with name {string} and ssn {string} on courses {string}")
    public void registerStudent(final String studentName, final String socialSecurityNumber, final String courses)
    {
        final String url = "/students/register";

        final List<CourseHttpRequest> coursesRequested = Arrays.stream(courses.split(","))
            .map(CourseHttpRequest::new)
            .collect(Collectors.toList());

        final StudentHttpRequest s = new StudentHttpRequest(studentName, socialSecurityNumber, coursesRequested);

        final StudentResponse registeredStudent = template.postForObject(url, s, StudentResponse.class);
        assertEquals(registeredStudent.getName(), studentName);

        final List<String> registeredCourseNames = registeredStudent.getCourses().stream()
            .map(course -> course.getName())
            .collect(Collectors.toList());

        assertThat(registeredCourseNames)
            .containsExactlyInAnyOrder(courses.split(","));
    }

    @When("we submit a request to delete student with ssn {string}")
    public void deleteStudent(final String socialSecurityNumber)
    {
        final String url = "/students/{ssn}";
        final ResponseEntity<Void> outcome = template.exchange(url, HttpMethod.DELETE, null, Void.class, socialSecurityNumber);
        assertTrue(outcome.getStatusCode().is2xxSuccessful());
    }

    @Then("the student with ssn {string} and her courses registrations are deleted")
    public void checkStudentAndRegistrationsDeleted(final String socialSecurityNumber)
    {
        final String url = "/students/{ssn}";
        final ResponseEntity<Student> student = template.getForEntity(url, Student.class, socialSecurityNumber);
        assertTrue(student.getStatusCode().equals(HttpStatus.NOT_FOUND));
    }

    @When("we request the list of students enrolled to course {string} we receive:")
    public void requestListOfEnrolledStudents(final String courseName, final DataTable dataTable)
    {
        final String url = "/students/listEnrolledStudents?courseName={name}";
        final ResponseEntity<List<LightweightStudentResponse>> student = template.exchange(url, HttpMethod.GET, null,
            new ParameterizedTypeReference<>()
            {
            }, courseName);
        final List<String> expectedStudentNames = dataTable.asMaps().stream()
            .map(entry -> entry.get("studentName"))
            .collect(Collectors.toList());
        List<String> studentNames = student.getBody().stream()
            .map(resp -> resp.getName())
            .collect(Collectors.toList());
        assertThat(studentNames)
            .containsExactly(expectedStudentNames.toArray(String[]::new));
    }


    class StudentHttpRequest
    {
        final String name;
        final String socialSecurityNumber;
        final List<CourseHttpRequest> courses;

        public StudentHttpRequest(final String name, final String socialSecurityNumber, final List<CourseHttpRequest> courses)
        {
            this.name = name;
            this.socialSecurityNumber = socialSecurityNumber;
            this.courses = courses;
        }

        public String getName()
        {
            return name;
        }

        public List<CourseHttpRequest> getCourses()
        {
            return courses;
        }

        public String getSocialSecurityNumber()
        {
            return socialSecurityNumber;
        }
    }

    class CourseHttpRequest
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
}
