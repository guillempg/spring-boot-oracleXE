package com.example.springjpaoracle;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.RabbitMQContainer;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    private final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    private final OracleContainer oracle = new OracleContainer("oracle/database:18.4.0-xe")
            .withFileSystemBind("oracle18.4.0XE", "/opt/oracle/oradata", BindMode.READ_WRITE)
            .withExposedPorts(1521, 5500)
            .withUsername("testuser")
            .withPassword("testpassword");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startRabbitMQ(applicationContext);
        startOracleDb(applicationContext);
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
