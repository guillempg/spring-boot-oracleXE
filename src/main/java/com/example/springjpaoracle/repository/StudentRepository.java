package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer>
{
    @Query(value = "SELECT s FROM Course c INNER JOIN c.students as s WHERE c.name = ?1 ORDER BY s.keycloakId ASC")
    List<Student> findStudentsByCoursesNameIgnoreCase(String courseName);

    @Transactional
    void deleteByKeycloakId(String keycloakId);

    Optional<Student> findByKeycloakId(String keycloakId);

    @Query(value = "SELECT s from Student s WHERE s.keycloakId not in (select s2.keycloakId from Student s2 JOIN s2.courses c where upper(c.name)=upper(?1))")
    List<Student> findStudentsNotRegisteredToCourse(String courseName);
}
