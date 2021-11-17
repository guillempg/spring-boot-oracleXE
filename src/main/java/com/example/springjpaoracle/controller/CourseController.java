package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.dto.*;
import com.example.springjpaoracle.model.TeacherAssignation;
import com.example.springjpaoracle.service.StudentService;
import com.example.springjpaoracle.service.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController
{
    private final StudentService studentService;
    private final TeacherService teacherService;

    public CourseController(final StudentService studentService, final TeacherService teacherService)
    {
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    @PostMapping(value = "/score")
    public ResponseEntity<StudentCourseScoreResponse> score(@RequestBody ScoreRequest score)
    {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        score.setTeacherKeycloakId(authentication.getName());
        final var savedScore = studentService.score(score);
        final var resp = StudentCourseScoreResponse.from(savedScore);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PostMapping(value = "/assignteacher")
    public ResponseEntity<TeacherAssignationResponse> assignTeacher(@RequestBody AssignTeacherRequest assignTeacherRequest)
    {
        final TeacherAssignation assignation = teacherService.assignTeacher(assignTeacherRequest);
        return new ResponseEntity<>(TeacherAssignationResponse.from(assignation), HttpStatus.OK);
    }

    @PostMapping(value = "/saveteacher")
    public ResponseEntity<TeacherResponse> saveTeacher(@RequestBody SaveTeacherRequest saveTeacherRequest)
    {
        final var teacher = teacherService.findOrSaveTeacher(saveTeacherRequest);
        return new ResponseEntity<>(TeacherResponse.from(teacher), HttpStatus.OK);
    }

    @GetMapping(value = "/score/student/{studentKeycloakId}")
    public ResponseEntity<List<StudentCourseScoreResponse>> viewStudentScores(@PathVariable String studentKeycloakId)
    {
        final var scores = studentService.viewStudentScores(studentKeycloakId);
        final List<StudentCourseScoreResponse> resp = StudentCourseScoreResponse.from(scores);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @GetMapping(value = "/score/{courseName}")
    public ResponseEntity<List<StudentCourseScoreResponse>> viewStudentCourseScores(@PathVariable String courseName)
    {
        final var scores = studentService.viewCourseScores(courseName);
        final List<StudentCourseScoreResponse> resp = StudentCourseScoreResponse.from(scores);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
