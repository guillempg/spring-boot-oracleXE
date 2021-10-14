package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer>
{
    Optional<Course> findByNameIgnoreCase(String name);
}
