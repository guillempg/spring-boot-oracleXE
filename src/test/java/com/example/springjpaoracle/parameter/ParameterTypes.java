package com.example.springjpaoracle.parameter;

import com.example.springjpaoracle.client.KeycloakUserCache;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterTypes
{

    private final KeycloakUserCache keycloakClient;

    public ParameterTypes(final KeycloakUserCache keycloakClient)
    {
        this.keycloakClient = keycloakClient;
    }

    @DataTableType
    public StudentHttpRequest registrationDetail(Map<String, String> entry)
    {
        return new StudentHttpRequest()
                .setStudentKeycloakId(keycloakClient.getKeycloakIdByUsername(entry.get("name")).getId())
                .setCourseNames(courses(entry.get("courses")));
    }

    @ParameterType(name = "courses", value = ".*")
    public List<String> courses(String rawCourses)
    {
        return Arrays.stream(rawCourses.split(","))
                .map(String::trim).collect(Collectors.toList());
    }

    @DataTableType
    public TeacherDetail teacherDetail(Map<String, String> entry)
    {
        return new TeacherDetail()
                .setKeycloakId(keycloakClient.getKeycloakIdByUsername(entry.get("name")).getId())
                .setCourseNames(courses(entry.get("coursesAssigned")));
    }

    @DataTableType
    public RegisterScore registerScore(Map<String, String> entry)
    {
        return new RegisterScore()
                .setScore(Double.parseDouble(entry.get("score")))
                .setCourseName(entry.get("courseName"))
                .setKeycloakId(keycloakClient.getKeycloakIdByUsername(entry.get("studentName")).getId());
    }

    @DataTableType
    @ParameterType(name = "keycloakUser", value = ".*")
    public KeycloakUser keycloakUser(String username)
    {
        return keycloakClient.getKeycloakIdByUsername(username);
    }
}
