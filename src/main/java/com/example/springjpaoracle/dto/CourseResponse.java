package com.example.springjpaoracle.dto;

import com.example.springjpaoracle.model.Course;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse
{
    private String name;

    public static List<CourseResponse> from(List<Course> courses)
    {
        return courses.stream()
                .map(c -> new CourseResponse().setName(c.getName()))
                .collect(Collectors.toList());
    }
}
