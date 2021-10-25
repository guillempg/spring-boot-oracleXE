package com.example.springjpaoracle;

import com.example.springjpaoracle.exception.StudentNotFoundException;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KeycloakClientImpl implements KeycloakClient
{
    private final RestTemplate authTemplate;
    private final RestTemplate adminTemplate;
    private final String clientSecret;
    private final String clientId;

    public KeycloakClientImpl(final RestTemplate authTemplate,
                              final RestTemplate adminTemplate,
                              final String clientId,
                              final String clientSecret)
    {
        this.authTemplate = authTemplate;
        this.adminTemplate = adminTemplate;
        this.clientSecret = clientSecret;
        this.clientId = clientId;
    }

    @Override
    public String getAccessToken(final String username, final String password)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        map.add("client_secret", clientSecret);
        map.add("client_id", clientId);
        map.add("grant_type", "password");
        map.add("scope", "openid");

        ResponseEntity<Map> response =
                authTemplate.exchange("/protocol/openid-connect/token",
                        HttpMethod.POST,
                        new HttpEntity<>(map, headers),
                        Map.class);
        return (String) response.getBody().get("access_token");
    }

    @Override
    public String getKeycloakIdByUsername(final String username, final String token)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<List<KeycloakUser>> response =
                adminTemplate.exchange("/users?username=" + username,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<>()
                        {
                        });
        return Optional.ofNullable(response.getBody())
                .flatMap(body -> body.stream().findFirst())
                .orElseThrow(() -> new StudentNotFoundException(username))
                .getId();
    }

    @Override
    public String getUsernameByKeycloakId(final String keycloakId, final String token)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<KeycloakUser> response =
                adminTemplate.exchange("/users/{keycloakId}",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KeycloakUser.class,
                        keycloakId);
        return Optional.ofNullable(response.getBody())
                .map(body -> body.getUsername())
                .orElseThrow(() -> new StudentNotFoundException(keycloakId));
    }

    @Data
    static class KeycloakUser
    {
        private String id;
        private String username;
    }
}
