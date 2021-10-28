package com.example.springjpaoracle.client;

import com.example.springjpaoracle.parameter.KeycloakUser;

public interface KeycloakUserCache
{
    KeycloakUser getKeycloakIdByUsername(String username);

    void getKeycloakIdByUsernameAsAdmin(String admin, String username);
}
