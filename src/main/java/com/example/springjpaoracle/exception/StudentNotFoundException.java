package com.example.springjpaoracle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class StudentNotFoundException extends RuntimeException
{
    public StudentNotFoundException(final String socialSecurityNumber)
    {
        super(socialSecurityNumber);
    }
}
