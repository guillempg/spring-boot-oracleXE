package com.example.springjpaoracle.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.springjpaoracle.dto.LightweightStudentResponse;
import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<StudentResponse> createStudent(@RequestBody Student student)
    {
        final var savedStudent = studentService.registerStudent(student);
        final StudentResponse resp = StudentResponse.from(savedStudent);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @GetMapping(value = "/listStudentByName")
    public ResponseEntity<List<StudentResponse>> listStudentsByName(@RequestParam final String name)
    {

        List<Student> students = studentService.findByNameIgnoreCase(name);
        final List<StudentResponse> lightweightStudentRespons = students.stream()
            .map(s -> StudentResponse.from(s))
            .collect(Collectors.toList());

        return new ResponseEntity<>(lightweightStudentRespons, HttpStatus.OK);
    }

    @GetMapping(value = "/listEnrolledStudents")
    public ResponseEntity<List<LightweightStudentResponse>> listStudentsEnrolledToCourseName(@RequestParam final String courseName)
    {
        List<Student> students = studentService.findStudentsByCoursesNameIgnoreCase(courseName);
        final List<LightweightStudentResponse> lightweightStudentRespons = students.stream()
            .map(s -> LightweightStudentResponse.from(s))
            .collect(Collectors.toList());
        return new ResponseEntity<>(lightweightStudentRespons, HttpStatus.OK);
    }

    @DeleteMapping("{socialSecurityNumber}")
    public ResponseEntity<Void> delete(@PathVariable final String socialSecurityNumber)
    {
        studentService.deleteBySocialSecurityNumber(socialSecurityNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{socialSecurityNumber}")
    public ResponseEntity<LightweightStudentResponse> findBySocialSecurityNumber(@PathVariable final String socialSecurityNumber)
    {
        return studentService.findBySocialSecurityNumber(socialSecurityNumber)
            .map(LightweightStudentResponse::from)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new StudentNotFoundException(socialSecurityNumber));
    }
}
