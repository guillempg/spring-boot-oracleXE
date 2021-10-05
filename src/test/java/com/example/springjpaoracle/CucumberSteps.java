package com.example.springjpaoracle;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.springjpaoracle.dto.StudentResponse;

import org.json.JSONException;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class CucumberSteps
{
    private final ConfigurableApplicationContext context;
    private final TestRestTemplate template;

    public CucumberSteps(final ConfigurableApplicationContext context, final TestRestTemplate template)
    {
        this.context = context;
        this.template = template;
    }

    @Given("the app is running")
    public void applicationIsRunning()
    {
        assertTrue(context.isRunning());
    }

    @When("we successfully register student with details:")
    public void registerStudents(io.cucumber.datatable.DataTable dataTable)
    {
        for (Map<String,String> row : dataTable.asMaps())
        {
            try
            {
                registerStudent(row.get("name"), row.get("courses"));
            }
            catch(Exception e)
            {
                fail("Student not registered: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //@When("we successfully register student {string} on courses {string}")
    private void registerStudent(final String studentName, final String courses) throws JSONException
    {
        final String url = "/students/register";

        final List<CourseHttpRequest> coursesRequested = Arrays.stream(courses.split(","))
            .map(CourseHttpRequest::new)
            .collect(Collectors.toList());

        final StudentHttpRequest s = new StudentHttpRequest(studentName, coursesRequested);

        final StudentResponse registeredStudent = template.postForObject(url, s, StudentResponse.class);
        assertEquals(registeredStudent.getName(),studentName);

        final List<String> registeredCourseNames = registeredStudent.getCourses().stream()
            .map(course -> course.getName())
            .collect(Collectors.toList());

        assertThat(registeredCourseNames)
            .containsExactlyInAnyOrder(courses.split(","));
    }

    class StudentHttpRequest{
        final String name;
        final List<CourseHttpRequest> courses;

        public StudentHttpRequest(final String name, final List<CourseHttpRequest> courses)
        {
            this.name = name;
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
    }

    class CourseHttpRequest {
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
