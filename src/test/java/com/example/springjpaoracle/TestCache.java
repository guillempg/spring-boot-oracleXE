package com.example.springjpaoracle;

import java.util.HashMap;
import java.util.Map;

public class TestCache
{
    private final KeycloakClient keycloakClient;
    private final Map<String, String> tokenByUsername = new HashMap<>();
    private final Map<String, String> passwordByUsername = new HashMap<>();
    private final Map<String, String> keycloakIdByUsername = new HashMap<>();

    public TestCache(final KeycloakClient keycloakClient)
    {
        this.keycloakClient = keycloakClient;
    }

    public String findUsernameByKeycloakId(final String keycloakId, final String adminUsername)
    {
        return keycloakIdByUsername.entrySet().stream()
                .filter(entry -> entry.getValue().equals(keycloakId))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElseGet(() -> keycloakClient.getUsernameByKeycloakId(keycloakId, this.getToken(adminUsername)));
    }

    public String getKeycloakIdByUsername(final String adminUser, final String studentUsername)
    {
        return keycloakIdByUsername.computeIfAbsent(studentUsername, k -> keycloakClient.getKeycloakIdByUsername(
                studentUsername,
                this.getToken(adminUser)));
    }

    public String getToken(final String username)
    {
        return tokenByUsername.get(username);
    }

    public String getToken(final String username, final String password)
    {
        return tokenByUsername.computeIfAbsent(username, k -> keycloakClient.getAccessToken(
                username,
                password));
    }
}
