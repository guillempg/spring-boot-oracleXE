package com.example.springjpaoracle.dto;

import java.util.List;

import com.example.springjpaoracle.model.Student;

public class StudentResponse
{
    private String name;
    private List<CourseResponse> courses;

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setCourses(final List<CourseResponse> courses)
    {
        this.courses = courses;
    }

    public String getName()
    {
        return name;
    }

    public List<CourseResponse> getCourses()
    {
        return courses;
    }

    public static StudentResponse from(final Student student)
    {
        List<CourseResponse> courses = CourseResponse.from(student.getCourses());
        final StudentResponse r = new StudentResponse();
        r.setName(student.getName());
        r.setCourses(courses);
        return r;
    }
}

