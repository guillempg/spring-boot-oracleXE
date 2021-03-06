package com.example.springjpaoracle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Getter
@Setter
@Entity
@Accessors(chain = true)
@ToString(callSuper = true)
@Table(name = "PERSON")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Role")
public abstract class Person extends BaseEntity
{
    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String keycloakId;
}
