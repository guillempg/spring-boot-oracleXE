package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "TEACHER")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class Teacher implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String keycloakId;

    @OneToMany(mappedBy = "relatedStudent")
    private List<Phone> phoneNumbers;

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
