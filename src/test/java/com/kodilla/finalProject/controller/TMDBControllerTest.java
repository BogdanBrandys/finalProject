package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.repository.MovieRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.repository.UserRepository;
import com.kodilla.finalProject.security.JwtUtility;
import com.kodilla.finalProject.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
class TMDBControllerTest {

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
    private User user;

    @BeforeEach
    void setUp() {

        // User
        user = new User();
        user.setFirst_name("John");
        user.setLast_name("Doe");
        user.setUsername("john.doe");
        user.setEmail("john.doe@test.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(List.of(new Role(1L, "USER")));
        user.setFavoriteMovies(new ArrayList<>());

        userRepository.save(user);

        userToken = jwtUtility.generateToken(user.getUsername());

    }

    @Test
    void shouldSearchMoviesAndAddToFavorites() throws Exception {
        //Given
        String title = "Tomb Raider";
        //When&Then
        MvcResult searchResult = mockMvc.perform(get("/v1/tmdb/search")
                        .param("title", title)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = searchResult.getResponse().getContentAsString();
        List<MovieBasicDTO> movies = objectMapper.readValue(searchResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MovieBasicDTO.class));

        assertThat(movies).isNotEmpty();
        assertThat(movies.get(0).getTitle()).containsIgnoringCase(title);
        assertThat(movies.get(0).getId()).isNotNull();
        assertThat(movies.get(0).getRelease_date()).isNotNull();

        assertThat(movies.get(0).getTitle()).isEqualToIgnoringCase("Tomb Raider");
    }
}