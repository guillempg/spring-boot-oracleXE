package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer>
{
    Optional<Course> findByNameIgnoreCase(String name);

    List<Course> findByNameIgnoreCaseIn(List<String> name);
}
