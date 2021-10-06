package com.example.springjpaoracle;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

@TestConfiguration
public class TestConfig
{
    @Bean
    public TestRestTemplate testRestTemplate(){
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:8080"));
    }

    @Bean
    public CompositeRepository uberRepository(List<CrudRepository<?,?>> allRepositories)
    {
        return () -> allRepositories.forEach(CrudRepository::deleteAll);
    }
}
