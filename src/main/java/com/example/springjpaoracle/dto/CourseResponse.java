package com.example.springjpaoracle.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse
{
    private String name;
}
