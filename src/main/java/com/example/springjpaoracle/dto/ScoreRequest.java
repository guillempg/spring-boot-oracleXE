package com.example.springjpaoracle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreRequest
{
    private double score;
    private String courseName;
    private String studentSocialSecurityNumber;
}
