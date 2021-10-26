package com.example.springjpaoracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AssignTeacherRequest
{
    private String teacherKeycloakId;
    private String courseName;
}
