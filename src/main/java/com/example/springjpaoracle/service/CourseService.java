package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.CourseHttpRequest;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.repository.CourseRepository;

import java.util.List;

public class CourseService
{
    private final CourseRepository courseRepository;

    public CourseService(final CourseRepository courseRepository)
    {
        this.courseRepository = courseRepository;
    }

    public Course create(final CourseHttpRequest request)
    {
        return courseRepository.save(new Course().setName(request.getName()).setDescription(request.getDescription()));
    }

    public List<Course> listCourses()
    {
        return courseRepository.findAll();
    }
}
