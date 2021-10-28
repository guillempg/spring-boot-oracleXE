package com.example.springjpaoracle.client;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

public class ApplicationClientImpl implements ApplicationClient
{
    private final String registrationId;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final WebTestClient webTestClient;
    private final OAuth2AuthorizedClientManager manager;
    private final OAuth2AuthorizeRequest.Builder requestBuilder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final JwtDecoder jwtDecoder;

    public ApplicationClientImpl(final String registrationId,
                                 final OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                 final WebTestClient webTestClient,
                                 final OAuth2AuthorizedClientManager manager,
                                 final JwtAuthenticationConverter jwtAuthenticationConverter,
                                 final JwtDecoder jwtDecoder)
    {
        this.registrationId = registrationId;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.webTestClient = webTestClient;
        this.manager = manager;
        this.requestBuilder = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId);
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public AbstractAuthenticationToken getAccessToken(final String username, final String password)
    {
        OAuth2AuthorizeRequest request = requestBuilder.principal(username)
                .attribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username)
                .attribute(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password).build();
        return Optional.ofNullable(manager.authorize(request))
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(AbstractOAuth2Token::getTokenValue)
                .map(jwtDecoder::decode)
                .map(jwtAuthenticationConverter::convert)
                .orElseThrow();
    }

    @Override
    public OAuth2AuthorizedClient getAuthorizedClient(final String username)
    {
        return oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId, username);
    }

    @Override
    public WebTestClient getWebTestClient()
    {
        return this.webTestClient;
    }
}
