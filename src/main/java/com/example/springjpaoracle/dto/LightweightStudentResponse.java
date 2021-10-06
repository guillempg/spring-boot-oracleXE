package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Student;

public class LightweightStudentResponse
{
    private String name;
    private String socialSecurityNumber;

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static LightweightStudentResponse from(Student student)
    {
        final LightweightStudentResponse studentResponse = new LightweightStudentResponse();
        studentResponse.setName(student.getName());
        studentResponse.setSocialSecurityNumber(student.getSocialSecurityNumber());
        return studentResponse;
    }

    public String getSocialSecurityNumber()
    {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(final String socialSecurityNumber)
    {
        this.socialSecurityNumber = socialSecurityNumber;
    }
}
