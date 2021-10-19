package com.example.springjpaoracle.auth0;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    public void configure(HttpSecurity http) throws Exception
    {
        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        final CustomJwtConverter converter = new CustomJwtConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
        //final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        //jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("resource_access");//.springjpaoracle.roles");

        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/courses").permitAll()
                .mvcMatchers(HttpMethod.DELETE, "/students/**").hasAuthority("admin")
                .mvcMatchers(HttpMethod.POST, "/students/**").hasAuthority("admin")
                .mvcMatchers(HttpMethod.GET, "/students/**").hasAuthority("user")
                //.mvcMatchers("/api/private-scoped").hasAuthority("SCOPE_read:messages")
                .and().cors()
                .and().oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter);
    }
}
