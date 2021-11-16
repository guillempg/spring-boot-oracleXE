package com.example.springjpaoracle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
public class RedisTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    private final GenericContainer<?> redis = new GenericContainer<>("redis:5.0.3-alpine")
            .withLogConsumer(new Slf4jLogConsumer(log))
            .withExposedPorts(6379);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        redis.start();
        System.setProperty("REDIS_HOST", redis.getHost());
        System.setProperty("REDIS_PORT", redis.getMappedPort(6379).toString());

        log.info("Redis started. host:" + redis.getHost() + " and port: " + redis.getMappedPort(6379).toString());
    }
}
