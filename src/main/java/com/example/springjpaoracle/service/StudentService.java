package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.dto.ScoreRequest;
import com.example.springjpaoracle.exception.CourseNotFoundException;
import com.example.springjpaoracle.exception.StudentNotFoundException;
import com.example.springjpaoracle.exception.StudentRegistrationNotFoundException;
import com.example.springjpaoracle.exception.TeacherNotFoundException;
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
    private final TeacherRepository teacherRepository;
    private final PhoneRepository phoneRepository;
    private final StudentCourseScoreRepository scoreRepository;
    private final MeterRegistry registry;
    private final StudentRegistrationRepository studentRegistrationRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, final TeacherRepository teacherRepository, final PhoneRepository phoneRepository, final StudentCourseScoreRepository scoreRepository, final MeterRegistry registry, final StudentRegistrationRepository studentRegistrationRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
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
                .orElseThrow(() -> new StudentNotFoundException("Student with keycloakId:" + scoreRequest.getStudentKeycloakId() + " not found"));
        final Teacher teacher = teacherRepository.findByKeycloakId(scoreRequest.getTeacherKeycloakId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher with keycloakId:" + scoreRequest.getTeacherKeycloakId() + " not found"));
        final Course course = courseRepository.findByNameIgnoreCase(scoreRequest.getCourseName())
                .orElseThrow(() -> new CourseNotFoundException("Course with name " + scoreRequest.getCourseName() + " not found"));
        final StudentRegistration registration = studentRegistrationRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new StudentRegistrationNotFoundException(student.getId(), course.getId()));

        score.setScore(scoreRequest.getScore());
        score.setRegistration(registration);
        score.setTeacher(teacher);

        return scoreRepository.save(score);
    }

    public List<StudentCourseScore> viewStudentScores(final String studentKeycloakId)
    {
        return scoreRepository.viewStudentCourseScores(studentKeycloakId);
    }

    public List<StudentCourseScore> viewCourseScores(final String courseName)
    {
        return scoreRepository.viewCourseScores(courseName);
    }
}