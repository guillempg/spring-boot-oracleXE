package com.example.springjpaoracle;

import com.example.springjpaoracle.config.AllTestContainersInitializer;
import com.example.springjpaoracle.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class, TestConfig.class}, initializers = {AllTestContainersInitializer.class})
public class CucumberTestConfig
{
}
