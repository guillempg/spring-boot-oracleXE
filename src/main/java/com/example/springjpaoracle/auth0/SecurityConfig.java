package com.example.springjpaoracle.auth0;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        final CustomJwtConverter converter = new CustomJwtConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtAuthenticationConverter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/courses").permitAll()
                .mvcMatchers(HttpMethod.POST, "/courses/score").hasAuthority("teacher")
                .mvcMatchers(HttpMethod.GET, "/courses/score/student/**").hasAnyAuthority("student", "teacher")
                .mvcMatchers(HttpMethod.GET, "/courses/score/**").hasAuthority("teacher")
                .mvcMatchers(HttpMethod.POST, "/courses/assignteacher").hasAuthority("admin")
                .mvcMatchers(HttpMethod.GET, "/students/**").hasAnyAuthority("admin", "user")
                .mvcMatchers(HttpMethod.POST, "/students/**").hasAuthority("admin")
                .mvcMatchers(HttpMethod.DELETE, "/students/**").hasAuthority("admin")
                .and().cors()
                .and().oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());
    }

    @Bean
    public ResourceOwnerOrGrantedAuthority isOwnerOrAnyAuthorities()
    {
        return new ResourceOwnerOrGrantedAuthority(() -> SecurityContextHolder.getContext().getAuthentication());
    }
}
