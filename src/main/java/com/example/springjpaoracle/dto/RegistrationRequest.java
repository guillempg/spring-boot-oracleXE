package com.example.springjpaoracle.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RegistrationRequest
{
    private String studentKeycloakId;
    private List<String> courseNames;
}
