package com.example.springjpaoracle;

import com.example.springjpaoracle.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@Lazy
@TestConfiguration
public class TestConfig
{
    @Bean
    public TestRestTemplate applicationRestTemplate(@Value("${local.server.port}") int localServerPort)
    {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + localServerPort));
    }

    @Bean
    public KeycloakClient keycloakRestTemplate(@Value("${application.keycloak_auth_root_uri}") String keycloakAuthRootUri,
                                               @Value("${application.keycloak_admin_root_uri}") String keycloakAdminRootUri,
                                               @Value("${application.client_id}") String clientId,
                                               @Value("${application.client_secret}") String clientSecret)
    {
        return new KeycloakClientImpl(
                new RestTemplateBuilder().rootUri(keycloakAuthRootUri).build(),
                new RestTemplateBuilder().rootUri(keycloakAdminRootUri).build(),
                clientId,
                clientSecret);
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
}
