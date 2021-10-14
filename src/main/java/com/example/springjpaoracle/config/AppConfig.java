package com.example.springjpaoracle.config;

import com.example.springjpaoracle.repository.CourseRepository;
import com.example.springjpaoracle.repository.PhoneRepository;
import com.example.springjpaoracle.repository.StudentCourseScoreRepository;
import com.example.springjpaoracle.repository.StudentRepository;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{

    @Bean
    public StudentService studentService(StudentRepository studentRepository,
                                         CourseRepository courseRepository,
                                         PhoneRepository phoneRepository,
                                         StudentCourseScoreRepository scoreRepository)
    {
        return new StudentService(studentRepository, courseRepository, phoneRepository, scoreRepository);
    }
}
