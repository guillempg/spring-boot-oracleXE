package com.example.springjpaoracle;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    private final OracleContainer oracle = new OracleContainer("oracle/database:18.4.0-xe")
            .withFileSystemBind("oracle18.4.0XE", "/opt/oracle/oradata", BindMode.READ_WRITE)
            .withExposedPorts(1521, 5500)
            .withUsername("testuser")
            .withPassword("testpassword");

    private final GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("jboss/keycloak:15.0.2"))
            //.withCommand("bin/standalone.sh -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/opt/jboss/keycloak/imports/realm-export.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING")
            .withFileSystemBind("keycloak", "/opt/jboss/keycloak/imports")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_USER", "admin")
            .withEnv("KEYCLOAK_PASSWORD", "admin")
            .withEnv("KEYCLOAK_IMPORT", "/opt/jboss/keycloak/imports/realm-export.json");


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startKeycloak(applicationContext);
        startOracleDb(applicationContext);
        startRabbitMQ(applicationContext);
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
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + issuerUrl
        ).applyTo(applicationContext.getEnvironment());
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
}
