package com.example.springjpaoracle.config;

import com.example.springjpaoracle.controller.CourseRepository;
import com.example.springjpaoracle.controller.StudentRepository;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public StudentService studentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        return new StudentService(studentRepository, courseRepository);
    }
}
