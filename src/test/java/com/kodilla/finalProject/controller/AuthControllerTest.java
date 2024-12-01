package com.kodilla.finalProject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.repository.UserRepository;
import com.kodilla.finalProject.security.JwtUtility;
import com.kodilla.finalProject.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirst_name("Clare");
        user.setLast_name("Red");
        user.setUsername("user1");
        user.setEmail("user@test.pl");
        user.setPassword(passwordEncoder.encode("userpassword"));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(List.of(new Role(1L, "USER")));
        userRepository.save(user);

        userToken = jwtUtility.generateToken(user.getUsername());
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("user1", "userpassword");

        // When & Then
        mockMvc.perform(post("/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("user1", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}