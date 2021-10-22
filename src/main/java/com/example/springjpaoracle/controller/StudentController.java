package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.dto.LightweightStudentResponse;
import com.example.springjpaoracle.dto.ScoreRequest;
import com.example.springjpaoracle.dto.StudentCourseScoreResponse;
import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.exception.StudentNotFoundException;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
public class StudentController
{
    private final StudentService studentService;

    public StudentController(final StudentService studentService)
    {
        this.studentService = studentService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<StudentResponse> registerStudent(@RequestBody Student student)
    {
        final var savedStudent = studentService.registerStudent(student);
        final StudentResponse resp = StudentResponse.from(savedStudent);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PostMapping(value = "/score")
    public ResponseEntity<StudentCourseScoreResponse> score(@RequestBody ScoreRequest score)
    {
        final var savedScore = studentService.score(score);
        final StudentCourseScoreResponse resp = StudentCourseScoreResponse.from(savedScore);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @GetMapping(value = "/listEnrolledStudents")
    public ResponseEntity<List<LightweightStudentResponse>> listStudentsEnrolledToCourseName(@RequestParam final String courseName)
    {
        List<Student> students = studentService.findStudentsByCoursesNameIgnoreCase(courseName);
        final List<LightweightStudentResponse> lightweightStudentResponse = students.stream()
                .map(LightweightStudentResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(lightweightStudentResponse, HttpStatus.OK);
    }

    @DeleteMapping("{keycloakId}")
    public ResponseEntity<Void> delete(@PathVariable final String keycloakId)
    {
        studentService.deleteByKeycloakId(keycloakId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{keycloakId}")
    @PreAuthorize("@isOwnerOrAnyAuthorities.validate(#keycloakId, 'admin')")
    public ResponseEntity<LightweightStudentResponse> findByKeycloakId(@PathVariable final String keycloakId)
    {
        return studentService.findByKeycloakId(keycloakId)
                .map(LightweightStudentResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new StudentNotFoundException(keycloakId));
    }

    @GetMapping(value = "/listStudentsNotEnrolled")
    public ResponseEntity<List<LightweightStudentResponse>> listStudentsNotEnrolledToCourseName(@RequestParam final String courseName)
    {
        List<Student> studentsNotRegisteredToCourse = studentService.findStudentsNotRegisteredToCourse(courseName);
        final List<LightweightStudentResponse> lightweightStudentResponse = studentsNotRegisteredToCourse.stream()
                .map(s -> LightweightStudentResponse.from(s))
                .collect(Collectors.toList());
        return new ResponseEntity<>(lightweightStudentResponse, HttpStatus.OK);
    }
}
