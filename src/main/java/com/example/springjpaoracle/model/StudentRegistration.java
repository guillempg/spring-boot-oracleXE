package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(
        name = "STUDENT_COURSE_REGISTER",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"STUDENT_ID", "COURSE_ID"})}
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class StudentRegistration extends BaseEntity
{
    @ManyToOne
    @JoinColumn(name = "STUDENT_ID")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "COURSE_ID")
    private Course course;
}
