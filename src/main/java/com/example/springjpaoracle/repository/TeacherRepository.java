package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer>
{
    Optional<Teacher> findByKeycloakId(String teacherKeycloakId);
}
