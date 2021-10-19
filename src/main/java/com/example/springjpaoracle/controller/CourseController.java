package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.dto.CourseResponse;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController
{
    private final StudentService studentService;

    public CourseController(final StudentService studentService)
    {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses()
    {
        final List<CourseResponse> coursesResponse = studentService.findAllCourses().stream()
                .map(CourseResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(coursesResponse, HttpStatus.OK);
    }
}
