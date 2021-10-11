package com.example.springjpaoracle;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;

@Lazy
@TestConfiguration
public class TestConfig
{
    @Bean
    public TestRestTemplate testRestTemplate(@Value("${local.server.port}") int localServerPort) {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + localServerPort));
    }

    @Bean
    public CompositeRepository uberRepository(List<CrudRepository<?,?>> allRepositories)
    {
        return () -> allRepositories.forEach(CrudRepository::deleteAll);
    }
}
