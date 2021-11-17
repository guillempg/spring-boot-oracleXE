package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.model.StudentRegistration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class StudentResponse
{
    private List<CourseResponse> courses;
    private String keycloakId;

    public static StudentResponse from(Student student)
    {
        final var studentResponse = new StudentResponse();
        studentResponse.setKeycloakId(student.getKeycloakId());

        studentResponse.setCourses(student.getRegistrations().stream()
                .map(StudentRegistration::getCourse)
                .map(c -> new CourseResponse().setName(c.getName()))
                .collect(Collectors.toList()));

        return studentResponse;
    }
}
