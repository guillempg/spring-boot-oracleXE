package com.example;

//import org.h2.server.web.WebServlet;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;


@Configuration
public class BeanConfig {

	//@Bean
    //ServletRegistrationBean h2servletRegistration(){
    //    ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
    //    registrationBean.addUrlMappings("/console/*");
    //    return registrationBean;
    //}

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    DataSource oracleXE() throws SQLException
    {
        final Properties properties = new Properties();
        properties.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME,dbUsername);
        properties.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, dbPassword);
        properties.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        OracleDataSource ds = new OracleDataSource();
        ds.setURL(jdbcUrl);
        ds.setConnectionProperties(properties);

        return ds;
    }

}
