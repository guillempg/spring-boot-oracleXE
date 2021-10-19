package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.ScoreRequest;
import com.example.springjpaoracle.exception.CourseNotFoundException;
import com.example.springjpaoracle.exception.StudentNotFoundException;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Phone;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.model.StudentCourseScore;
import com.example.springjpaoracle.repository.CourseRepository;
import com.example.springjpaoracle.repository.PhoneRepository;
import com.example.springjpaoracle.repository.StudentCourseScoreRepository;
import com.example.springjpaoracle.repository.StudentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentService
{
    public static final String REGISTER_STUDENT_REQUEST_COUNT = "registerStudentRequestCount";
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PhoneRepository phoneRepository;
    private final StudentCourseScoreRepository scoreRepository;
    private final MeterRegistry registry;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, final PhoneRepository phoneRepository, final StudentCourseScoreRepository scoreRepository, final MeterRegistry registry)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.phoneRepository = phoneRepository;
        this.scoreRepository = scoreRepository;
        this.registry = registry;
    }

    @Transactional
    public Student registerStudent(Student student)
    {
        registry.counter(REGISTER_STUDENT_REQUEST_COUNT).increment();
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

    public List<Course> findAllCourses()
    {
        return courseRepository.findAll();
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

    public StudentCourseScore score(final ScoreRequest scoreRequest)
    {
        final StudentCourseScore score = new StudentCourseScore();
        final Student student = studentRepository.findBySocialSecurityNumber(scoreRequest.getStudentSocialSecurityNumber())
                .orElseThrow(() -> new StudentNotFoundException("Student with ssn:" + scoreRequest.getStudentSocialSecurityNumber() + " not found"));
        final Course course = courseRepository.findByNameIgnoreCase(scoreRequest.getCourseName())
                .orElseThrow(() -> new CourseNotFoundException("Course with name " + scoreRequest.getCourseName() + " not found"));

        score.setScore(scoreRequest.getScore());
        score.setStudent(student);
        score.setCourse(course);

        return scoreRepository.save(score);
    }
}