package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Course;

public class CourseResponse
{
    private String name;

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static CourseResponse from(Course course)
    {
        CourseResponse r = new CourseResponse();
        r.setName(course.getName());
        return r;
    }
}
