package com.example.springjpaoracle.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseHttpRequest
{
    private String name;
    private String description;
}
