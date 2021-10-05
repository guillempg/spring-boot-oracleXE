package com.example.springjpaoracle.model;

import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student
{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @OneToOne
    @JoinColumn(name="USER_DETAILS_ID")
    private UserDetails personalData;

    @ManyToMany
    @JoinTable(
        name="STUDENT_COURSE_REGISTER",
        joinColumns = @JoinColumn(name="STUDENT_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name="COURSE_ID", referencedColumnName = "ID")
    )

    private List<Course> courses;

    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public UserDetails getPersonalData()
    {
        return personalData;
    }

    public void setPersonalData(final UserDetails personalData)
    {
        this.personalData = personalData;
    }

    public List<Course> getCourses()
    {
        return courses;
    }

    public void setCourses(final List<Course> courses)
    {
        this.courses = courses;
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
        Student student = (Student)o;
        return id == student.id &&
            Objects.equals(name, student.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }
}
