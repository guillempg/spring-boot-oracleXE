package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Student;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LightweightStudentResponse
{
    private String name;
    private String socialSecurityNumber;

    public static LightweightStudentResponse from(Student student)
    {
        final LightweightStudentResponse studentResponse = new LightweightStudentResponse();
        studentResponse.setName(student.getName());
        studentResponse.setSocialSecurityNumber(student.getSocialSecurityNumber());
        return studentResponse;
    }
}
