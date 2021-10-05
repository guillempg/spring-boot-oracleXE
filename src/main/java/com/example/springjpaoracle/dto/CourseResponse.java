package com.example.springjpaoracle.dto;

import java.util.ArrayList;
import java.util.List;

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

    public static List<CourseResponse> from(List<Course> courses)
    {
        final List<CourseResponse> courseResponseList = new ArrayList<>();
        for (Course course : courses)
        {
            final CourseResponse r = new CourseResponse();
            r.setName(course.getName());
            courseResponseList.add(r);
        }
        return courseResponseList;
    }
}
