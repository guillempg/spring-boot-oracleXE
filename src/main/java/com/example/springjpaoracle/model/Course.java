package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "COURSE")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class Course extends BaseEntity
{
    @Column
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "courses")
    private List<Teacher> teachers;
}
