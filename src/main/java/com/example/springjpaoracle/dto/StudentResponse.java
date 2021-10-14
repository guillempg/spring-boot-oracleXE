package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Student;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class StudentResponse
{
    private String name;
    private List<CourseResponse> courses;
    private String socialSecurityNumber;

    public static StudentResponse from(Student student)
    {
        final StudentResponse studentResponse = new StudentResponse();
        studentResponse.setName(student.getName());
        studentResponse.setSocialSecurityNumber(student.getSocialSecurityNumber());
        studentResponse.setCourses(student.getCourses().stream()
                .map(CourseResponse::from)
                .collect(Collectors.toList()));

        return studentResponse;
    }
}
