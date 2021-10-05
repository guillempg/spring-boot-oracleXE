package com.example.springjpaoracle.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.springjpaoracle.dto.StudentResponse;
import com.example.springjpaoracle.model.Course;
import com.example.springjpaoracle.model.Student;

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
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentController(final StudentRepository studentRepository, final CourseRepository courseRepository)
    {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<StudentResponse> createStudent(@RequestBody Student student) {

        List<Course> courses = findOrCreate(student.getCourses());
        student.setCourses(courses);

        final var savedStudent =  studentRepository.save(student);
        final StudentResponse resp = StudentResponse.from(savedStudent);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    private List<Course> findOrCreate(final List<Course> courses)
    {
        return courses.stream()
            .map((course) -> courseRepository.findByNameIgnoreCase(
                course.getName())
                .orElseGet(()-> courseRepository.save(course)))
        .collect(Collectors.toList());
    }

    @GetMapping(value = "/listStudentByName")
    public ResponseEntity<List<Student>> listStudentsByName(@RequestParam final String name) {

        List<Student> students = studentRepository.findByNameIgnoreCase(name);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping(value = "/listEnrolledStudents")
    public ResponseEntity<List<Student>> listStudentsEnrolledToCourseName(@RequestParam final String courseName) {

        List<Student> students = studentRepository.findStudentsByCoursesNameIgnoreCase(courseName);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @DeleteMapping("{studentId}")
    public ResponseEntity<Void> delete(@PathVariable final int studentId){

        studentRepository.deleteById(studentId);
        return ResponseEntity.ok().build();

    }
}
