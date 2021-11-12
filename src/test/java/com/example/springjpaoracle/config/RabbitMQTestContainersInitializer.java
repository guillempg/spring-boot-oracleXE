package com.example.springjpaoracle.config;

import com.example.springjpaoracle.RabbitMQSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.RabbitMQContainer;

@Slf4j
public class RabbitMQTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-alpine")
            .withExposedPorts(5672, 15672);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startRabbitMQ(applicationContext);
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
}
