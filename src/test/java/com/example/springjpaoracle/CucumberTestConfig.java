package com.example.springjpaoracle;

import com.example.springjpaoracle.config.TestConfig;
import com.example.springjpaoracle.config.TestContainersInitializer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class, TestConfig.class}, initializers = {TestContainersInitializer.class})
public class CucumberTestConfig {
}
