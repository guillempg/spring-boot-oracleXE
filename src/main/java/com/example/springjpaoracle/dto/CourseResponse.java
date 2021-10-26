package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Course;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
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
