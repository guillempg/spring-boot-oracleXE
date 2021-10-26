package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.TeacherAssignation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherAssignationRepository extends JpaRepository<TeacherAssignation, Integer>
{
    Optional<TeacherAssignation> findByTeacherIdAndCourseName(Integer teacherKeycloakId, String courseName);
}
