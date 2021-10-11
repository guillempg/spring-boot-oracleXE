package com.example.springjpaoracle.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "PHONE")
public class Phone
{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relatedStudentId")
    private Student relatedStudent;

    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public Student getRelatedStudent()
    {
        return relatedStudent;
    }

    public void setRelatedStudent(final Student relatedStudent)
    {
        this.relatedStudent = relatedStudent;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return id == phone.id &&
                phoneNumber.equals(phone.phoneNumber);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, phoneNumber);
    }
}
