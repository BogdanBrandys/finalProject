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
class AdminControllerTest {

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

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Admin in database
        User admin = new User();
        admin.setFirst_name("Tom");
        admin.setLast_name("Bob");
        admin.setUsername("admin8");
        admin.setEmail("admin@test.pl");
        admin.setPassword(passwordEncoder.encode("adminpassword"));
        admin.setStatus(User.UserStatus.ACTIVE);
        admin.setRoles(List.of(new Role(1L, "ADMIN")));
        userRepository.save(admin);

        // token for admin
        adminToken = jwtUtility.generateToken(admin.getUsername());
    }

    @Test
    void getUsers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUserById() throws Exception {
        //Given
        User user = new User();
        user.setFirst_name("John");
        user.setLast_name("Doe");
        user.setUsername("john.doe");
        user.setEmail("john.doe@test.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(List.of(new Role(1L, "USER")));
        userRepository.save(user);
        //When&Then
        mockMvc.perform(get("/v1/admin/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    void createUserByAdmin_shouldCreateUser() throws Exception {
        //Given
        UserDTO userDto = new UserDTO("Alice", "Smith", "alice.smith@test.pl", "alice.smith", "password", List.of(new RoleDTO("USER")));
        //When&Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/v1/admin/users/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_shouldUpdateUser() throws Exception {
        //Given
        UserDTO userDto = new UserDTO("Alice", "Smith", "alice.smith@test.pl", "alice.smith", "password", List.of(new RoleDTO("USER")));
        Long userId = 1L;

        //When&Then
        mockMvc.perform(put("/v1/admin/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value(userDto.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(userDto.getLast_name()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.username").value(userDto.getUsername()));
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        //Given
        User user = new User();
        user.setFirst_name("John");
        user.setLast_name("Doe");
        user.setUsername("john.doe");
        user.setEmail("john.doe@test.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(List.of(new Role(1L, "USER")));
        user.setUserMovies(new ArrayList<>());

        user = userRepository.save(user);
        Long userId = user.getId();

        // When&Then
        mockMvc.perform(delete("/v1/admin/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

