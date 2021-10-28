package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Course;
import lombok.*;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
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
