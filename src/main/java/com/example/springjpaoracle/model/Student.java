package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "STUDENT")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class Student extends BaseEntity
{
    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String keycloakId;

    @OneToMany(mappedBy = "relatedStudent")
    private List<Phone> phoneNumbers;

    @OneToMany(mappedBy = "student", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<StudentRegistration> registrations;
}
