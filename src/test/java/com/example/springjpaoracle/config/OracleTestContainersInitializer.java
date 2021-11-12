package com.example.springjpaoracle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.OracleContainer;

@Slf4j
public class OracleTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    private final OracleContainer oracle;

    public OracleTestContainersInitializer()
    {
        this.oracle = new OracleContainer("gvenzl/oracle-xe:18.4.0-slim")
                .withUsername("testuser")
                .withPassword("testpassword")
                .withNetworkAliases("oracle")
                .withEnv("ORACLE_PASSWORD", "oracle")
                .withFileSystemBind("oracle_init", "/container-entrypoint-initdb.d", BindMode.READ_WRITE)
                .withExposedPorts(1521, 5500);
    }

    public OracleTestContainersInitializer(final Network network)
    {
        this();
        oracle.withNetwork(network);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        startOracleDb(applicationContext);
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
