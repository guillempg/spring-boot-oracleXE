package com.example.springjpaoracle.messaging;

import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class MessagingConfig
{
    @Bean
    public Consumer<Message<RegistrationRequest>> studentRegistryInput(StudentService studentService)
    {
        return message -> studentService.registerStudent(message.getPayload());
    }

    @Bean
    public Consumer<String> studentDeleteInput(StudentService studentService)
    {
        return studentService::deleteByKeycloakId;
    }
}
