package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Student;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer>
{
    String STUDENTS_BY_KEYCLOAK_ID = "studentsByKeycloakId";

    @Query(value = "SELECT s FROM Student s INNER JOIN s.registrations as r INNER JOIN r.course as c WHERE c.name = ?1 ORDER BY s.keycloakId ASC")
    List<Student> findStudentsByCoursesNameIgnoreCase(String courseName);

    @CachePut(value = STUDENTS_BY_KEYCLOAK_ID, key = "#entity.keycloakId")
    <S extends Student> S save(S entity);

    @Transactional
    @CacheEvict(value = STUDENTS_BY_KEYCLOAK_ID, key = "#keycloakId")
    void deleteByKeycloakId(String keycloakId);

    @Cacheable(value = STUDENTS_BY_KEYCLOAK_ID,key = "#keycloakId")
    Optional<Student> findByKeycloakId(String keycloakId);

    @Query(value = "SELECT s from Student s WHERE s.keycloakId not in (select s2.keycloakId from Student s2 INNER JOIN s2.registrations r INNER JOIN r.course as c where upper(c.name)=upper(?1))")
    List<Student> findStudentsNotRegisteredToCourse(String courseName);
}
