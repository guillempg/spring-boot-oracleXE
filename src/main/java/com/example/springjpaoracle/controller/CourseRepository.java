package com.example.springjpaoracle.controller;

import java.util.Optional;

import com.example.springjpaoracle.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Integer>
{
    Optional<Course> findByNameIgnoreCase(String name);
}
