package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest
{
    private static final String EXISTING_KEYCLOAK_ID = "123456";
    private static final String NOT_EXISTING_KEYCLOAK_ID = "9999999";
    private static final String STUDENT_NAME = "any";

    @Autowired
    private MockMvc mockedMvc;

    @MockBean
    private StudentService studentService;

    @Test
    @Disabled
    void shouldFindByKeycloakId() throws Exception
    {
        when(studentService.findByKeycloakId(EXISTING_KEYCLOAK_ID))
                .thenReturn(
                        Optional.of(new Student()
                                .setCourses(new ArrayList<>())
                                .setKeycloakId(EXISTING_KEYCLOAK_ID)));

        mockedMvc.perform(get("/students/{keycloakId}", EXISTING_KEYCLOAK_ID))
                .andDo(print())
                .andExpect(jsonPath("$.keycloakId").value(EXISTING_KEYCLOAK_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled
    void shouldReturn404WhenNotFoundByKeycloakId() throws Exception
    {
        when(studentService.findByKeycloakId(NOT_EXISTING_KEYCLOAK_ID))
                .thenReturn(
                        Optional.empty());

        mockedMvc.perform(get("/students/{keycloakId}", NOT_EXISTING_KEYCLOAK_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}