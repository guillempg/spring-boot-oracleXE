package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "COURSE")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class Course
{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "courses", cascade = CascadeType.REMOVE)
    private List<Student> students;

    @ManyToMany(mappedBy = "courses", cascade = CascadeType.REMOVE)
    private List<Teacher> teachers;

    @OneToMany(mappedBy = "course")
    private List<StudentCourseScore> scores;
}
