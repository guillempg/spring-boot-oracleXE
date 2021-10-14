package com.example.springjpaoracle.service;

import com.example.springjpaoracle.controller.CourseRepository;
import com.example.springjpaoracle.controller.PhoneRepository;
import com.example.springjpaoracle.controller.StudentRepository;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Phone;
import com.example.springjpaoracle.model.Student;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentService
{
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PhoneRepository phoneRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, final PhoneRepository phoneRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.phoneRepository = phoneRepository;
    }

    @Transactional
    public Student registerStudent(Student student)
    {
        List<Course> courses = findOrCreateCourses(student.getCourses());
        student.setCourses(courses);
        List<Phone> phones = findOrCreatePhones(student.getPhoneNumbers());
        student.setPhoneNumbers(phones);
        return studentRepository.save(student);
    }

    private List<Course> findOrCreateCourses(final List<Course> courses)
    {
        return courses.stream()
                .map((course) -> courseRepository.findByNameIgnoreCase(course.getName())
                        .orElseGet(() -> courseRepository.save(course)))
                .collect(Collectors.toList());
    }

    private List<Phone> findOrCreatePhones(final List<Phone> phones)
    {
        if (phones != null)
        {
            return phones.stream()
                    .map((phone) -> phoneRepository.findByPhoneNumberIgnoreCase(phone.getPhoneNumber())
                            .orElseGet(() -> phoneRepository.save(phone)))
                    .collect(Collectors.toList());
        } else
        {
            return Collections.emptyList();
        }
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

    public List<Student> findStudentsNotRegisteredToCourse(String courseName)
    {
        return studentRepository.findStudentsNotRegisteredToCourse(courseName);
    }
}