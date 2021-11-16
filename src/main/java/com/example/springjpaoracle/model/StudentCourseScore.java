package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "STUDENT_COURSE_SCORE")
@EqualsAndHashCode
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class StudentCourseScore implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column
    private Double score;

    @ManyToOne
    @JoinColumn(name = "REGISTRATION_ID")
    private StudentRegistration registration;

    @ManyToOne
    @JoinColumn(name = "TEACHER_ID")
    private Teacher teacher;

}
