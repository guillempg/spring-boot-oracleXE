package com.example.springjpaoracle;

public interface KeycloakClient
{
    String getAccessToken(String username, String password);

    String getKeycloakIdByUsername(String username, String token);

    String getUsernameByKeycloakId(String keycloakId, String token);
}
