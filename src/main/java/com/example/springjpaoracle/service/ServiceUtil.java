package com.example.springjpaoracle.service;

import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.repository.CourseRepository;

import java.util.List;

public final class ServiceUtil
{
    private ServiceUtil()
    {
    }

    public static List<Course> findOrCreateCourses(final CourseRepository courseRepository, final List<String> courseNames)
    {
        return courseNames.stream()
                .map(courseName -> courseRepository.findByNameIgnoreCase(courseName)
                        .orElseGet(() -> courseRepository.save(new Course().setName(courseName))))
                .toList();
    }
}
