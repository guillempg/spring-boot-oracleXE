package com.example.springjpaoracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final Network network = Network.newNetwork();

    private final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    private final OracleContainer oracle = new OracleContainer("oracle/database:18.4.0-xe")
            .withNetwork(network)
            .withNetworkAliases("oracle")
            .withFileSystemBind("oracle18.4.0XE", "/opt/oracle/oradata", BindMode.READ_WRITE)
            .withUsername("testuser")
            .withPassword("testpassword")
            .withExposedPorts(1521, 5500);

    private final GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("jboss/keycloak:15.0.2"))
            .withNetwork(network)
            .withFileSystemBind("keycloak/realm-export.json", "/opt/jboss/keycloak/imports/realm-export.json")
            .withFileSystemBind("keycloak/ojdbc8.jar", "/opt/jboss/keycloak/modules/system/layers/base/com/oracle/jdbc/main/driver/ojdbc.jar")
            .withExposedPorts(8080)
            .withEnv("JAVA_OPTS_APPEND", "-Dkeycloak.profile.feature.upload_scripts=enabled")
            .withEnv("KEYCLOAK_IMPORT", "/opt/jboss/keycloak/imports/realm-export.json")
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


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startRabbitMQ(applicationContext);
        startOracleDb(applicationContext);
        startKeycloak(applicationContext);
    }

    private void startRabbitMQ(ConfigurableApplicationContext applicationContext)
    {
        rabbitMQContainer.start();
        TestPropertyValues.of(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
                "spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword()
        ).applyTo(applicationContext.getEnvironment());

        resetRabbitMQ(applicationContext);
    }

    private void resetRabbitMQ(final ConfigurableApplicationContext applicationContext)
    {
        applicationContext.getBeanFactory().registerSingleton("rabbitMQSupport", (RabbitMQSupport) () ->
        {
            try
            {
                rabbitMQContainer.execInContainer("sh", "/opt/rabbitmq/sbin/rabbitmqctl", "stop_app");
                rabbitMQContainer.execInContainer("sh", "/opt/rabbitmq/sbin/rabbitmqctl", "reset");
                rabbitMQContainer.execInContainer("sh", "/opt/rabbitmq/sbin/rabbitmqctl", "start_app");
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    private void startOracleDb(ConfigurableApplicationContext applicationContext)
    {
        oracle.start();
        TestPropertyValues.of(
                "spring.datasource.url=" + oracle.getJdbcUrl(),
                "spring.datasource.username=" + oracle.getUsername(),
                "spring.datasource.password=" + oracle.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }

    private void startKeycloak(final ConfigurableApplicationContext applicationContext)
    {

        keycloak.start();
        final Integer mappedPort = keycloak.getMappedPort(8080);
        final String host = keycloak.getHost();
        final String certsUrl = String.format("http://%s:%d/auth/realms/springjpaoracle/protocol/openid-connect/certs", host, mappedPort);
        final String issuerUrl = String.format("http://%s:%d/auth/realms/springjpaoracle", host, mappedPort);
        TestPropertyValues.of(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + certsUrl,
                "application.keycloak_root_uri=" + issuerUrl,
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + issuerUrl
        ).applyTo(applicationContext.getEnvironment());
    }
}
