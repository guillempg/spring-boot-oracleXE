package com.example.springjpaoracle.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "COURSE")
public class Course
{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "courses", cascade = CascadeType.REMOVE)
    private List<Student> students;

    public int getId()
    {
        return id;
    }

    public Course setId(final int id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public Course setName(final String name)
    {
        this.name = name;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public Course setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public List<Student> getStudents()
    {
        return students;
    }

    public Course setStudents(final List<Student> students)
    {
        this.students = students;
        return this;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Course course = (Course) o;
        return id == course.id &&
                Objects.equals(name, course.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }
}
