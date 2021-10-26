package com.example.springjpaoracle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TeacherAssignationException extends RuntimeException
{
    public TeacherAssignationException(final String teacherKeycloakId, final String courseName)
    {
        super(String.format("TeacherAssignation failed, either teacher: %s or course: %s not found", teacherKeycloakId, courseName));
    }
}
