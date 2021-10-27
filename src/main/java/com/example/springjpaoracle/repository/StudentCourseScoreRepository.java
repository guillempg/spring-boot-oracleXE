package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.StudentCourseScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentCourseScoreRepository extends JpaRepository<StudentCourseScore, Integer>
{
    @Query("SELECT score FROM StudentCourseScore score INNER JOIN score.registration as r INNER JOIN r.course as c INNER JOIN r.student as s WHERE s.keycloakId = ?1")
    List<StudentCourseScore> viewStudentCourseScores(String studentKeycloakId);

    @Query("SELECT score FROM StudentCourseScore score INNER JOIN score.registration as r INNER JOIN r.course as c INNER JOIN r.student as s WHERE c.name = ?1")
    List<StudentCourseScore> viewCourseScores(String courseName);
}
