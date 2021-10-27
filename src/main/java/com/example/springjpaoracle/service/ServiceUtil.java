package com.example.springjpaoracle.service;

import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.repository.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceUtil
{
    public static List<Course> findOrCreateCourses(final CourseRepository courseRepository, final List<String> courseNames)
    {
        return courseNames.stream()
                .map((courseName) -> courseRepository.findByNameIgnoreCase(courseName)
                        .orElseGet(() -> courseRepository.save(new Course().setName(courseName))))
                .collect(Collectors.toList());
    }
}
