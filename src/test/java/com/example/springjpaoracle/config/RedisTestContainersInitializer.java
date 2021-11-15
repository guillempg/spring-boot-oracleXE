package com.example.springjpaoracle.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class RedisTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    private final GenericContainer redis = new GenericContainer("redis:5.0.3-alpine")
        .withExposedPorts(6379);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        redis.start();
        System.setProperty("spring.redis.host", redis.getHost());
        System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());

        System.out.println("Redis started. host:" + redis.getHost() + " and port: " + redis.getMappedPort(6379).toString());
    }
}
