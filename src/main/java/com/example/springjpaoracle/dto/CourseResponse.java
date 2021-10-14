package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Course;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CourseResponse
{
    private String name;

    public static CourseResponse from(Course course)
    {
        CourseResponse r = new CourseResponse();
        r.setName(course.getName());
        return r;
    }
}
