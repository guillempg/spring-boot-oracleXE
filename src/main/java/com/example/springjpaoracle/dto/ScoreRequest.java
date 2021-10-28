package com.example.springjpaoracle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ScoreRequest
{
    private double score;
    private String courseName;
    private String studentKeycloakId;
    private String teacherKeycloakId;
}
