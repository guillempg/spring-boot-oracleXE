package com.example.springjpaoracle.service;

import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.dto.ScoreRequest;
import com.example.springjpaoracle.exception.*;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.model.StudentCourseScore;
import com.example.springjpaoracle.model.StudentRegistration;
import com.example.springjpaoracle.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class StudentService
{
    public static final String REGISTER_STUDENT_REQUEST_COUNT = "registerStudentRequestCount";
    public static final String NOT_FOUND = " not found";
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentCourseScoreRepository scoreRepository;
    private final MeterRegistry registry;
    private final StudentRegistrationRepository studentRegistrationRepository;
    private final TeacherAssignationRepository teacherAssignationRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, final TeacherRepository teacherRepository, final StudentCourseScoreRepository scoreRepository, final MeterRegistry registry, final StudentRegistrationRepository studentRegistrationRepository, final TeacherAssignationRepository teacherAssignationRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.scoreRepository = scoreRepository;
        this.registry = registry;
        this.studentRegistrationRepository = studentRegistrationRepository;
        this.teacherAssignationRepository = teacherAssignationRepository;
    }

    @Transactional
    public Student registerStudent(RegistrationRequest registrationRequest)
    {
        registry.counter(REGISTER_STUDENT_REQUEST_COUNT).increment();
        var student = new Student().setKeycloakId(registrationRequest.getStudentKeycloakId());
        List<StudentRegistration> registrations = ServiceUtil.findOrCreateCourses(
                        courseRepository,
                        registrationRequest.getCourseNames()).stream()
                .map(course -> new StudentRegistration().setCourse(course).setStudent(student))
                .collect(Collectors.toList());
        student.setRegistrations(registrations);

        //List<Phone> phones = findOrCreatePhones(registrationRequest.getPhoneNumbers());
        //registrationRequest.setPhoneNumbers(phones);
        return studentRepository.save(student);
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
        final var score = new StudentCourseScore();
        final var student = studentRepository.findByKeycloakId(scoreRequest.getStudentKeycloakId())
                .orElseThrow(() -> new StudentNotFoundException("Student with keycloakId:" + scoreRequest.getStudentKeycloakId() + NOT_FOUND));
        final var teacher = teacherRepository.findByKeycloakId(scoreRequest.getTeacherKeycloakId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher with keycloakId:" + scoreRequest.getTeacherKeycloakId() + NOT_FOUND));
        final var course = courseRepository.findByNameIgnoreCase(scoreRequest.getCourseName())
                .orElseThrow(() -> new CourseNotFoundException("Course with name " + scoreRequest.getCourseName() + NOT_FOUND));
        final StudentRegistration registration = studentRegistrationRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new StudentRegistrationNotFoundException(student.getId(), course.getId()));
        if (teacherAssignationRepository.findByTeacherIdAndCourseName(teacher.getId(), course.getName()).isEmpty())
        {
            throw new TeacherAssignationException(teacher.getKeycloakId(), course.getName());
        }

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
