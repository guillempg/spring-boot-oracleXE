package com.example.springjpaoracle.client;

import com.example.springjpaoracle.parameter.KeycloakUser;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

public class KeycloakUserCacheImpl implements KeycloakUserCache
{
    private final Map<String, KeycloakUser> keycloakIdByUsername = new ConcurrentHashMap<>();
    private final String keycloakAdminRootUri;
    private final ApplicationClient applicationClient;

    public KeycloakUserCacheImpl(final String keycloakAdminRootUri,
                                 final ApplicationClient applicationClient)
    {
        this.keycloakAdminRootUri = keycloakAdminRootUri;
        this.applicationClient = applicationClient;
    }

    @Override
    public KeycloakUser getKeycloakIdByUsername(final String username)
    {
        return keycloakIdByUsername.get(username);
    }

    @Override
    public void getKeycloakIdByUsernameAsAdmin(final String admin, final String username)
    {
        keycloakIdByUsername.computeIfAbsent(username, usernameKey ->
                applicationClient.getWebTestClient().get().uri(keycloakAdminRootUri.concat("/users?username={username}"), usernameKey)
                        .attributes(oauth2AuthorizedClient(applicationClient.getAuthorizedClient(admin)))
                        .exchange().expectBody(new ParameterizedTypeReference<List<KeycloakUser>>()
                        {
                        }).returnResult().getResponseBody().get(0));
    }
}
