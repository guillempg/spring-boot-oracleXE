package com.example.springjpaoracle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class KeycloakTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final GenericContainer<?> keycloak;

    public KeycloakTestContainersInitializer()
    {
        keycloak = new GenericContainer<>(DockerImageName.parse("jboss/keycloak:15.0.2"))
                .withFileSystemBind("keycloak/export/kcdump.json", "/tmp/kcdump.json")
                .withFileSystemBind("keycloak/ojdbc8.jar", "/opt/jboss/keycloak/modules/system/layers/base/com/oracle/jdbc/main/driver/ojdbc.jar")
                .withExposedPorts(8080)
                .withEnv("JAVA_OPTS_APPEND", "-Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/kcdump.json")
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("DB_VENDOR", "oracle")
                .withEnv("DB_ADDR", "oracle")
                .withEnv("DB_DATABASE", "XE")
                .withEnv("DB_USER", "keycloak")
                .withEnv("DB_PASSWORD", "keycloak")
                .withEnv("DB_PORT", "1521")
                .waitingFor(Wait.forHttp("/")
                        .forStatusCode(200)
                        .forStatusCode(301)
                        .withStartupTimeout(Duration.ofMinutes(5)));
    }

    public KeycloakTestContainersInitializer(Network network)
    {
        this();
        keycloak.withNetwork(network);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startKeycloak(applicationContext);
    }

    private void startKeycloak(final ConfigurableApplicationContext applicationContext)
    {

        keycloak.start();
        final Integer mappedPort = keycloak.getMappedPort(8080);
        final String host = keycloak.getHost();
        final String certsUrl = String.format("http://%s:%d/auth/realms/springjpaoracle/protocol/openid-connect/certs", host, mappedPort);
        final String issuerUrl = String.format("http://%s:%d/auth/realms/springjpaoracle", host, mappedPort);
        final String adminUrl = String.format("http://%s:%d/auth/admin/realms/springjpaoracle", host, mappedPort);
        TestPropertyValues.of(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + certsUrl,
                "application.keycloak_auth_root_uri=" + issuerUrl,
                "application.keycloak_admin_root_uri=" + adminUrl,
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + issuerUrl
        ).applyTo(applicationContext.getEnvironment());
    }
}
