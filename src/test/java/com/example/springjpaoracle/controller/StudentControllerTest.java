package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;
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
    private static final String EXISTING_SSN = "123456";
    private static final String NOT_EXISTING_SSN = "9999999";
    private static final String STUDENT_NAME = "any";

    @Autowired
    private MockMvc mockedMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void shouldFindBySocialSecurityNumber() throws Exception
    {
        when(studentService.findBySocialSecurityNumber(EXISTING_SSN))
                .thenReturn(
                        Optional.of(new Student()
                                .setName(STUDENT_NAME)
                                .setCourses(new ArrayList<>())
                                .setSocialSecurityNumber(EXISTING_SSN)));

        mockedMvc.perform(get("/students/{ssn}", EXISTING_SSN))
                .andDo(print())
                .andExpect(jsonPath("$.socialSecurityNumber").value(EXISTING_SSN))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenNotFoundBySocialSecurityNumber() throws Exception
    {
        when(studentService.findBySocialSecurityNumber(NOT_EXISTING_SSN))
                .thenReturn(
                        Optional.empty());

        mockedMvc.perform(get("/students/{ssn}", NOT_EXISTING_SSN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}