package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.StudentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRegistrationRepository extends JpaRepository<StudentRegistration, Integer>
{
    Optional<StudentRegistration> findByStudentIdAndCourseId(Integer studentId, Integer courseId);
}
