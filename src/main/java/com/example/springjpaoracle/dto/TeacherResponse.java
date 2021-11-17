package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Teacher;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherResponse
{
    private String keycloakId;

    public static TeacherResponse from(final Teacher teacher)
    {
        final var teacherResponse = new TeacherResponse();
        teacherResponse.setKeycloakId(teacher.getKeycloakId());
        return teacherResponse;
    }
}
