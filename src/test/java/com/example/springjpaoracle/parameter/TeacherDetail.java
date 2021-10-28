package com.example.springjpaoracle.parameter;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TeacherDetail
{
    private String keycloakId;
    private List<String> courseNames;
}
