package com.fidely.backend.api.controllers;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.infrastructure.security.JjwtEmailVerificationTokenGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JjwtEmailVerificationTokenGenerator
            emailVerificationTokenGenerator;

    @Test
    void shouldRegisterAndVerifyCustomer() throws Exception {
        String email = "integration-test@example.com";

        String registerResponse = mockMvc.perform(
                        post("/auth/register/customer")
                                .contentType("application/json")
                                .content("""
                                {
                                    "email": "integration-test@example.com",
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "phone": "+33612345678",
                                    "birthDate": "1990-01-01",
                                    "password": "Password123!"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = com.jayway.jsonpath.JsonPath
                .read(registerResponse, "$.id");

        String token =
                emailVerificationTokenGenerator.generate(
                        UUID.fromString(userId)
                );

        mockMvc.perform(
                        get("/auth/verify-email")
                                .param("token", token)
                )
                .andExpect(status().isOk());
    }
}