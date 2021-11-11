package com.example.springjpaoracle.messaging;

import com.example.springjpaoracle.dto.RegistrationRequest;
import com.example.springjpaoracle.service.StudentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;

import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@Disabled("Flyway bean requires database even though it's not needed in these tests, consider replace @SpringBootTest with something more specific for messaging")
class MessagingConfigTest
{

    @MockBean
    StudentService studentService;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    InputDestination inputDestination;

    @Test
    void shouldRegisterStudentWhenMessageReceived()
    {
        var registrationRequest = RegistrationRequest.builder()
                .studentKeycloakId("123456")
                .courseNames(List.of("Math"))
                .build();
        inputDestination.send(new GenericMessage<>(registrationRequest), "students.topic");

        verify(studentService).registerStudent(registrationRequest);
    }

    @Test
    void shouldDeleteStudentWhenMessageReceived()
    {
        String keycloakId = "123123";
        inputDestination.send(new GenericMessage<>(keycloakId), "student_delete.topic");

        verify(studentService).deleteByKeycloakId(keycloakId);
    }
}
