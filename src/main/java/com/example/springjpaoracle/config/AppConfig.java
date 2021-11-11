package com.example.springjpaoracle.config;

import com.example.springjpaoracle.repository.*;
import com.example.springjpaoracle.service.StudentService;
import com.example.springjpaoracle.service.TeacherService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{

    @Bean
    public StudentService studentService(StudentRepository studentRepository,
                                         CourseRepository courseRepository,
                                         PhoneRepository phoneRepository,
                                         StudentCourseScoreRepository scoreRepository,
                                         MeterRegistry registry,
                                         final StudentRegistrationRepository studentRegistrationRepository,
                                         final TeacherRepository teacherRepository,
                                         final TeacherAssignationRepository teacherAssignationRepository)
    {
        return new StudentService(studentRepository,
                courseRepository,
                teacherRepository,
                phoneRepository,
                scoreRepository,
                registry,
                studentRegistrationRepository,
                teacherAssignationRepository);
    }

    @Bean
    public TeacherService teacherService(final TeacherRepository teacherRepository,
                                         final CourseRepository courseRepository,
                                         final TeacherAssignationRepository teacherAssignationRepository)
    {
        return new TeacherService(teacherRepository, courseRepository, teacherAssignationRepository);
    }
}
