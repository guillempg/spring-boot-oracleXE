package com.example.springjpaoracle.repository;

import com.example.springjpaoracle.config.OracleTestContainersInitializer;
import com.example.springjpaoracle.config.RedisTestContainersInitializer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("slow")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = {OracleTestContainersInitializer.class, RedisTestContainersInitializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryBaseTest
{
}
