package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.StudentCourseScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseScoreRepository extends JpaRepository<StudentCourseScore, Integer>
{
}
