package com.example.springjpaoracle.parameter;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KeycloakUser
{
    private String id;
    private String username;
}
