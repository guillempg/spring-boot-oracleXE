package com.example.springjpaoracle.controller;

import com.example.springjpaoracle.model.Student;
import com.example.springjpaoracle.service.StudentService;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Autowired
    private MockMvc mockedMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldBeUnauthorizedWhenNoTokenIsPresent() throws Exception
    {
        mockedMvc.perform(get("/students/{keycloakId}", EXISTING_KEYCLOAK_ID))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(studentService);
    }

    @Test
    void shouldFindBySocialSecurityNumber() throws Exception
    {
        Jwt jwt = getTokenWithRoles(EXISTING_KEYCLOAK_ID, "user");

        when(studentService.findByKeycloakId(EXISTING_KEYCLOAK_ID))
                .thenReturn(Optional.of(new Student().setKeycloakId(EXISTING_KEYCLOAK_ID)));

        mockedMvc.perform(get("/students/{keycloakId}", EXISTING_KEYCLOAK_ID)
                        .header("Authorization", getAuthorizationHeader(jwt)))
                .andDo(print())
                .andExpect(jsonPath("$.keycloakId").value(EXISTING_KEYCLOAK_ID))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenNotFoundByKeycloakId() throws Exception
    {
        Jwt jwt = getTokenWithRoles(NOT_EXISTING_KEYCLOAK_ID, "user");

        when(studentService.findByKeycloakId(NOT_EXISTING_KEYCLOAK_ID)).thenReturn(Optional.empty());

        mockedMvc.perform(get("/students/{keycloakId}", NOT_EXISTING_KEYCLOAK_ID)
                        .header("Authorization", getAuthorizationHeader(jwt)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBeForbiddenWhenStudentIsNotOwner() throws Exception
    {
        Jwt jwt = getTokenWithRoles("other_student", "user");

        when(studentService.findByKeycloakId(EXISTING_KEYCLOAK_ID))
                .thenReturn(Optional.of(new Student().setKeycloakId(EXISTING_KEYCLOAK_ID)));

        mockedMvc.perform(get("/students/{keycloakId}", EXISTING_KEYCLOAK_ID)
                        .header("Authorization", getAuthorizationHeader(jwt)))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(studentService);
    }

    @Test
    void shouldBeOkWhenRequesterIsAdmin() throws Exception
    {
        Jwt jwt = getTokenWithRoles("AdminId", "admin");

        when(studentService.findByKeycloakId(EXISTING_KEYCLOAK_ID))
                .thenReturn(Optional.of(new Student().setKeycloakId(EXISTING_KEYCLOAK_ID)));

        mockedMvc.perform(get("/students/{keycloakId}", EXISTING_KEYCLOAK_ID)
                        .header("Authorization", getAuthorizationHeader(jwt)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value(EXISTING_KEYCLOAK_ID));
    }

    private Jwt getTokenWithRoles(final String subject, String... rawRoles)
    {
        JSONArray roles = new JSONArray();
        roles.addAll(Arrays.asList(rawRoles));
        JSONObject realm = new JSONObject();
        realm.appendField("roles", roles);
        JSONObject resourceAccess = new JSONObject();
        resourceAccess.appendField("springjpaoracle", realm);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claims(stringObjectMap -> stringObjectMap.put("resource_access", resourceAccess))
                .build();

        when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        return jwt;
    }

    private String getAuthorizationHeader(final Jwt jwt)
    {
        return "Bearer " + jwt.getTokenValue();
    }
}