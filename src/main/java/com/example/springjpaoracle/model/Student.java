package com.example.springjpaoracle.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@RequiredArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@DiscriminatorValue("Student")
public class Student extends Person
{
    @OneToMany(mappedBy = "student", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<StudentRegistration> registrations;
}
