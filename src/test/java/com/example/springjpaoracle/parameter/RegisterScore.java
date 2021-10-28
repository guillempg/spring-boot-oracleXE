package com.example.springjpaoracle.parameter;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RegisterScore
{
    private String keycloakId;
    private String courseName;
    private double score;
}
