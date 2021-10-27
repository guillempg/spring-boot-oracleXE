package com.example.springjpaoracle.exception;

public class StudentRegistrationNotFoundException extends RuntimeException
{
    public StudentRegistrationNotFoundException(final Integer studentId, final Integer courseId)
    {
        super(String.format("Student registration does not exist for studentId: %s and courseId: %s", studentId, courseId));
    }
}
