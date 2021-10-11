package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Integer>
{
    @Query(value = "FROM Phone ph WHERE ph.phoneNumber = ?1")
    Optional<Phone> findByPhoneNumberIgnoreCase(String phoneNumber);
}
