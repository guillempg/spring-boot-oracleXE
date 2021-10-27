package com.example.springjpaoracle.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegistrationRequest
{
    private String studentKeycloakId;
    private List<String> courseNames;
}
