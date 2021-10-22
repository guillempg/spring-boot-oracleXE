package com.example.springjpaoracle;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class KeycloakClientImpl implements KeycloakClient
{
    private final RestTemplate template;
    private final String clientSecret;
    private final String clientId;

    public KeycloakClientImpl(final RestTemplate template, final String clientId, final String clientSecret)
    {
        this.template = template;
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
                template.exchange("/protocol/openid-connect/token",
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

        ResponseEntity<Map> response =
                template.exchange("/users?username=" + username,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class);
        return (String) response.getBody().get("id");
    }
}
