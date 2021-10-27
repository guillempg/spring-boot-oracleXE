package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Student;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LightweightStudentResponse
{
    private String keycloakId;

    public static LightweightStudentResponse from(Student student)
    {
        final LightweightStudentResponse studentResponse = new LightweightStudentResponse();
        studentResponse.setKeycloakId(student.getKeycloakId());
        return studentResponse;
    }
}
