package com.example;

//import org.h2.server.web.WebServlet;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

import com.example.model.UserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;


@Configuration
public class BeanConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    DataSource dataSource()
    {
        final Properties properties = new Properties();
        properties.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME,dbUsername);
        properties.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, dbPassword);
        properties.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        OracleDataSource ds = null;
        try
        {
            ds = new OracleDataSource();
            ds.setURL(jdbcUrl);
            ds.setConnectionProperties(properties);

        }
        catch(Exception  e)
        {
        }
        return ds;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter()
    {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        return jpaVendorAdapter;
    }

    @Bean(name = "entityManagerFactory")
    public EntityManagerFactory entityManagerFactory()
    {
    	LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    	emf.setDataSource(dataSource());
    	emf.setJpaVendorAdapter(jpaVendorAdapter());
    	emf.setPackagesToScan("com.example");
    	emf.setPersistenceUnitName("spring-boot-oracle");
    	emf.afterPropertiesSet();
    	return emf.getObject();
    }
}
