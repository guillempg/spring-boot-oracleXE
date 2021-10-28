package com.example.springjpaoracle.client;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.test.web.reactive.server.WebTestClient;

public interface ApplicationClient
{
    AbstractAuthenticationToken getAccessToken(String username, String password);

    OAuth2AuthorizedClient getAuthorizedClient(String username);

    WebTestClient getWebTestClient();
}
