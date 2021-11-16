package com.example.springjpaoracle.config;

import com.example.springjpaoracle.CompositeRepository;
import com.example.springjpaoracle.client.ApplicationClient;
import com.example.springjpaoracle.client.ApplicationClientImpl;
import com.example.springjpaoracle.client.KeycloakUserCache;
import com.example.springjpaoracle.client.KeycloakUserCacheImpl;
import com.example.springjpaoracle.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.reactive.server.WebTestClient;

@Lazy
@TestConfiguration
public class TestConfig
{

    public static final String REGISTRATION_ID = "keycloak";

    @Bean
    public TestRestTemplate applicationRestTemplate(@Value("${local.server.port}") int localServerPort)
    {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + localServerPort));
    }

    @Bean
    public CompositeRepository uberRepository(TeacherAssignationRepository assignationRepository,
                                              StudentCourseScoreRepository scoreRepository,
                                              StudentRegistrationRepository registrationRepository,
                                              StudentRepository studentRepository,
                                              TeacherRepository teacherRepository,
                                              CourseRepository courseRepository)
    {
        return () ->
        {
            assignationRepository.deleteAll();
            scoreRepository.deleteAll();
            registrationRepository.deleteAll();
            studentRepository.deleteAll();
            teacherRepository.deleteAll();
            courseRepository.deleteAll();
        };
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(@Value("${application.keycloak_auth_root_uri}") String keycloakIssuerLocation,
                                                                     @Value("${application.client_id}") String clientId,
                                                                     @Value("${application.client_secret}") String clientSecret)
    {
        return new InMemoryClientRegistrationRepository(
                ClientRegistrations.fromIssuerLocation(keycloakIssuerLocation)
                        .registrationId(REGISTRATION_ID)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                        .build()
        );
    }

    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientRepository(final ClientRegistrationRepository clientRegistrationRepository)
    {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clients,
                                                                 OAuth2AuthorizedClientService authz)
    {
        AuthorizedClientServiceOAuth2AuthorizedClientManager defaultOAuth2AuthorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, authz);
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .refreshToken()
                .password()
                .build();
        defaultOAuth2AuthorizedClientManager.setAuthorizedClientProvider(provider);
        defaultOAuth2AuthorizedClientManager.setContextAttributesMapper(OAuth2AuthorizeRequest::getAttributes);
        return defaultOAuth2AuthorizedClientManager;
    }

    @Bean
    public WebTestClient webTestClient(@Value("${local.server.port}") int localServerPort,
                                       final OAuth2AuthorizedClientManager manager)
    {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        return WebTestClient.bindToServer(new ReactorClientHttpConnector())
                .baseUrl("http://localhost:" + localServerPort)
                .filter(oauth2)
                .build();
    }

    @Bean
    public ApplicationClient applicationClient(final OAuth2AuthorizedClientService clientService,
                                               final WebTestClient webTestClient,
                                               final OAuth2AuthorizedClientManager manager,
                                               final JwtAuthenticationConverter jwtAuthenticationConverter,
                                               final JwtDecoder jwtDecoder)
    {
        return new ApplicationClientImpl(REGISTRATION_ID,
                clientService,
                webTestClient,
                manager,
                jwtAuthenticationConverter,
                jwtDecoder);
    }

    @Bean
    public KeycloakUserCache keycloakUserCache(@Value("${application.keycloak_admin_root_uri}") String keycloakAdminRootUri,
                                               final ApplicationClient applicationClient)
    {
        return new KeycloakUserCacheImpl(keycloakAdminRootUri, applicationClient);
    }

    @Bean
    public RedisTemplate<String, Object> courseRedisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        final RedisTemplate<String, Object> stringCourseRedisTemplate = new RedisTemplate<>();
        stringCourseRedisTemplate.setConnectionFactory(redisConnectionFactory);

        stringCourseRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringCourseRedisTemplate.setValueSerializer(new RedisSerializer<Object>()
        {
            @Override
            public byte[] serialize(final Object o) throws SerializationException
            {
                return new byte[0];
            }

            @Override
            public Object deserialize(final byte[] bytes) throws SerializationException
            {
                return "";
            }
        });
        return stringCourseRedisTemplate;
    }
}
