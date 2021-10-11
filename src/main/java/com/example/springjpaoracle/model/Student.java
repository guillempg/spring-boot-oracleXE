package com.example.springjpaoracle.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

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

    @Column(unique = true, nullable = false)
    private String socialSecurityNumber;

    @OneToMany(mappedBy = "relatedStudent")
    private List<Phone> phoneNumbers;

    @OneToOne
    @JoinColumn(name = "USER_DETAILS_ID")
    private UserDetails personalData;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "STUDENT_COURSE_REGISTER",
            joinColumns = @JoinColumn(name = "STUDENT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "COURSE_ID", referencedColumnName = "ID")
    )
    private List<Course> courses;

    public int getId()
    {
        return id;
    }

    public Student setId(final int id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public Student setName(final String name)
    {
        this.name = name;
        return this;
    }

    public UserDetails getPersonalData()
    {
        return personalData;
    }

    public Student setPersonalData(final UserDetails personalData)
    {
        this.personalData = personalData;
        return this;
    }

    public List<Course> getCourses()
    {
        return courses;
    }

    public Student setCourses(final List<Course> courses)
    {
        this.courses = courses;
        return this;
    }

    public String getSocialSecurityNumber()
    {
        return socialSecurityNumber;
    }

    public Student setSocialSecurityNumber(final String socialSecurityNumber)
    {
        this.socialSecurityNumber = socialSecurityNumber;
        return this;
    }

    public List<Phone> getPhoneNumbers()
    {
        return phoneNumbers;
    }

    public void setPhoneNumbers(final List<Phone> phoneNumbers)
    {
        this.phoneNumbers = phoneNumbers;
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
        Student student = (Student) o;
        return id == student.id &&
                socialSecurityNumber.equals(student.socialSecurityNumber);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, socialSecurityNumber);
    }
}
