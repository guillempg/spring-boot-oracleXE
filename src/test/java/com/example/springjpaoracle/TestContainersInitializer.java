package com.example.springjpaoracle;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.RabbitMQContainer;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        startRabbitMQ(applicationContext);
    }

    private void startRabbitMQ(ConfigurableApplicationContext applicationContext) {
        rabbitMQContainer.start();
        TestPropertyValues.of(
                "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                "spring.rabbitmq.port=" + rabbitMQContainer.getAmqpPort(),
                "spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
                "spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}
