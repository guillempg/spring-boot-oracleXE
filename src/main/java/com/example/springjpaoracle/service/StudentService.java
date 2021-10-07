package com.example.springjpaoracle.service;

import com.example.springjpaoracle.controller.CourseRepository;
import com.example.springjpaoracle.controller.StudentRepository;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Student registerStudent(Student student) {
        List<Course> courses = findOrCreate(student.getCourses());
        student.setCourses(courses);
        return studentRepository.save(student);
    }

    private List<Course> findOrCreate(final List<Course> courses) {
        return courses.stream()
                .map((course) -> courseRepository.findByNameIgnoreCase(course.getName())
                        .orElseGet(() -> courseRepository.save(course)))
                .collect(Collectors.toList());
    }
}