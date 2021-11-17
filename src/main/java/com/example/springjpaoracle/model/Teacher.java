package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
@DiscriminatorValue("Teacher")
public class Teacher extends Person
{
    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "TEACHER_COURSE_ASSIGNATIONS",
            joinColumns = @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "COURSE_ID", referencedColumnName = "ID")
    )
    private List<Course> courses;

    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<TeacherAssignation> assignations;
}
