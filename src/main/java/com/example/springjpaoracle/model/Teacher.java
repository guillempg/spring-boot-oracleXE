package com.example.springjpaoracle.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@ToString(callSuper = true)
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
