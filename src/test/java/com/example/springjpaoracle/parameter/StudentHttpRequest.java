package com.example.springjpaoracle.parameter;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StudentHttpRequest
{

    String studentKeycloakId;
    List<String> courseNames;
    List<PhoneHttpRequest> phones;
}
