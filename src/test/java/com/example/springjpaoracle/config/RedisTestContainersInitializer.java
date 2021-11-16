package com.example.springjpaoracle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class RedisTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    private final GenericContainer redis = new GenericContainer("redis:5.0.3-alpine")
        .withExposedPorts(6379);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        redis.start();
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);

        TestPropertyValues.of(
                "spring.redis.host="+ host,
                "spring.redis.port="+ port
        ).applyTo(applicationContext.getEnvironment());

        log.info("Redis started with host {} and port {}", host, port);
    }
}
