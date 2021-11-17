package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.dto.LightweightStudentResponse;
import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.exception.StudentNotFoundException;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<StudentResponse> registerStudent(@RequestBody RegistrationRequest registrationRequest)
    {
        final var savedStudent = studentService.registerStudent(registrationRequest);
        final var resp = StudentResponse.from(savedStudent);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @GetMapping(value = "/listEnrolledStudents")
    public ResponseEntity<List<LightweightStudentResponse>> listStudentsEnrolledToCourseName(@RequestParam final String courseName)
    {
        List<Student> students = studentService.findStudentsByCoursesNameIgnoreCase(courseName);
        final List<LightweightStudentResponse> lightweightStudentResponse = students.stream()
                .map(LightweightStudentResponse::from)
                .toList();
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
                .map(LightweightStudentResponse::from)
                .toList();
        return new ResponseEntity<>(lightweightStudentResponse, HttpStatus.OK);
    }
}
