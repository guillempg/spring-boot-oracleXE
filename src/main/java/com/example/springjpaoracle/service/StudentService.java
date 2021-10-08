package com.example.springjpaoracle.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.springjpaoracle.controller.CourseRepository;
import com.example.springjpaoracle.controller.StudentRepository;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;

import org.springframework.transaction.annotation.Transactional;

public class StudentService
{
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Student registerStudent(Student student)
    {
        List<Course> courses = findOrCreate(student.getCourses());
        student.setCourses(courses);
        return studentRepository.save(student);
    }

    private List<Course> findOrCreate(final List<Course> courses)
    {
        return courses.stream()
            .map((course) -> courseRepository.findByNameIgnoreCase(course.getName())
                .orElseGet(() -> courseRepository.save(course)))
            .collect(Collectors.toList());
    }

    public List<Student> findByNameIgnoreCase(final String name)
    {
        return studentRepository.findByNameIgnoreCase(name);
    }

    public List<Student> findStudentsByCoursesNameIgnoreCase(final String courseName)
    {
        return studentRepository.findStudentsByCoursesNameIgnoreCase(courseName);
    }

    public void deleteBySocialSecurityNumber(final String socialSecurityNumber)
    {
        studentRepository.deleteBySocialSecurityNumber(socialSecurityNumber);
    }

    public Optional<Student> findBySocialSecurityNumber(final String socialSecurityNumber)
    {
        return studentRepository.findBySocialSecurityNumber(socialSecurityNumber);
    }
}