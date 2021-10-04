package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.UserDetails;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer>
{
}
