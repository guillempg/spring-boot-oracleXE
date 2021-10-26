package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.dto.ScoreRequest;
import com.example.springjpaoracle.exception.CourseNotFoundException;
import com.example.springjpaoracle.exception.StudentNotFoundException;
import com.example.springjpaoracle.exception.StudentRegistrationNotFoundException;
import com.example.springjpaoracle.model.*;
import com.example.springjpaoracle.repository.*;
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
    private final StudentRegistrationRepository studentRegistrationRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, final PhoneRepository phoneRepository, final StudentCourseScoreRepository scoreRepository, final MeterRegistry registry, final StudentRegistrationRepository studentRegistrationRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.phoneRepository = phoneRepository;
        this.scoreRepository = scoreRepository;
        this.registry = registry;
        this.studentRegistrationRepository = studentRegistrationRepository;
    }

    @Transactional
    public Student registerStudent(RegistrationRequest registrationRequest)
    {
        registry.counter(REGISTER_STUDENT_REQUEST_COUNT).increment();
        Student student = new Student().setKeycloakId(registrationRequest.getStudentKeycloakId());
        List<StudentRegistration> registrations = ServiceUtil.findOrCreateCourses(
                        courseRepository,
                        registrationRequest.getCourseNames()).stream()
                .map((course) -> new StudentRegistration().setCourse(course).setStudent(student))
                .collect(Collectors.toList());
        student.setRegistrations(registrations);

        //List<Phone> phones = findOrCreatePhones(registrationRequest.getPhoneNumbers());
        //registrationRequest.setPhoneNumbers(phones);
        return studentRepository.save(student);
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

    public List<Student> findStudentsByCoursesNameIgnoreCase(final String courseName)
    {
        return studentRepository.findStudentsByCoursesNameIgnoreCase(courseName);
    }

    public void deleteByKeycloakId(final String keycloakId)
    {
        studentRepository.deleteByKeycloakId(keycloakId);
    }

    public Optional<Student> findByKeycloakId(final String keycloakId)
    {
        return studentRepository.findByKeycloakId(keycloakId);
    }

    public List<Student> findStudentsNotRegisteredToCourse(String courseName)
    {
        return studentRepository.findStudentsNotRegisteredToCourse(courseName);
    }

    public StudentCourseScore score(final ScoreRequest scoreRequest)
    {
        final StudentCourseScore score = new StudentCourseScore();
        final Student student = studentRepository.findByKeycloakId(scoreRequest.getStudentKeycloakId())
                .orElseThrow(() -> new StudentNotFoundException("Student with ssn:" + scoreRequest.getStudentKeycloakId() + " not found"));
        final Course course = courseRepository.findByNameIgnoreCase(scoreRequest.getCourseName())
                .orElseThrow(() -> new CourseNotFoundException("Course with name " + scoreRequest.getCourseName() + " not found"));
        final StudentRegistration registration = studentRegistrationRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new StudentRegistrationNotFoundException(student.getId(), course.getId()));

        score.setScore(scoreRequest.getScore());
        score.setRegistration(registration);

        return scoreRepository.save(score);
    }
}