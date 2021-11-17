package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(
        name = "TEACHER_COURSE_ASSIGNATIONS",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"TEACHER_ID", "COURSE_ID"})}
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class TeacherAssignation extends BaseEntity
{
    @ManyToOne
    @JoinColumn(name = "TEACHER_ID")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "COURSE_ID")
    private Course course;
}
