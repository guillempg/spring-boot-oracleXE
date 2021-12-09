package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(name = "STUDENT_COURSE_SCORE")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class StudentCourseScore extends BaseEntity
{
    @Column
    private Double score;

    @ManyToOne
    @JoinColumn(name = "REGISTRATION_ID")
    private StudentRegistration registration;

    @ManyToOne
    @JoinColumn(name = "TEACHER_ID")
    private Teacher teacher;

}
