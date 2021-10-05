package com.example.springjpaoracle;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig
{
    //@LocalServerPort
    //@Value("${local.server.port}")
    //private int localServerPort;

    @Bean
    public TestRestTemplate testRestTemplate(){
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:8080"));
    }
}
