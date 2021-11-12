package com.example.springjpaoracle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.Network;

@Slf4j
public class AllTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final Network network = Network.newNetwork();

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext)
    {
        final OracleTestContainersInitializer oracle = new OracleTestContainersInitializer(network);
        final KeycloakTestContainersInitializer keycloak = new KeycloakTestContainersInitializer(network);
        final RabbitMQTestContainersInitializer rabbitMQ = new RabbitMQTestContainersInitializer();
        oracle.initialize(applicationContext);
        keycloak.initialize(applicationContext);
        rabbitMQ.initialize(applicationContext);
    }
}
