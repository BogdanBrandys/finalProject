package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.service.CollectionService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
class CollectionControllerTest {

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

        // User
        User user = new User();
        user.setFirst_name("John");
        user.setLast_name("Doe");
        user.setUsername("john.doe");
        user.setEmail("john.doe@test.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles(List.of(new Role(1L, "USER")));

        user.setFavoriteMovies(new ArrayList<>());
        // Movie 1 - "American Beauty"
        Movie movie1 = new Movie();
        movie1.setTmdbId(14L);

        // Creating details for movie1
        MovieDetails details1 = new MovieDetails();
        details1.setTitle("American Beauty");
        details1.setGenre("Drama");
        details1.setYear("1999");
        details1.setDirector("Sam Mendes");
        details1.setPlot("A sexually frustrated suburban father has a mid-life crisis after becoming infatuated with his daughter's best friend.");

        // Creating ratings for movie1
        List<Rating> ratings1 = new ArrayList<>();
        ratings1.add(new Rating(1L, "Internet Movie Database", "8.3/10"));
        ratings1.add(new Rating(2L,"Rotten Tomatoes", "87%"));
        ratings1.add(new Rating(3L,"Metacritic", "84/100"));
        details1.setRatings(ratings1);
        movie1.setDetails(details1);

        // Creating providers for movie1
        List<MovieProvider> providers1 = new ArrayList<>();
        providers1.add(new MovieProvider(1L,"Apple TV", MovieProvider.AccessType.RENTAL, movie1));
        providers1.add(new MovieProvider(2L,"Google Play Movies", MovieProvider.AccessType.PURCHASE, movie1));
        providers1.add(new MovieProvider(3L,"Amazon Video", MovieProvider.AccessType.SUBSCRIPTION, movie1));
        movie1.setProviders(providers1);

        // Movie 2 - "The Dark Knight"
        Movie movie2 = new Movie();
        movie2.setTmdbId(15L);

        // Creating details for movie2
        MovieDetails details2 = new MovieDetails();
        details2.setTitle("The Dark Knight");
        details2.setGenre("Action");
        details2.setYear("2008");
        details2.setDirector("Christopher Nolan");
        details2.setPlot("When the menace known as The Joker emerges from his mysterious past, he wreaks havoc and chaos on the people of Gotham.");

        // Creating ratings for movie2
        List<Rating> ratings2 = new ArrayList<>();
        ratings2.add(new Rating(4L,"Internet Movie Database", "9.0/10"));
        ratings2.add(new Rating(5L,"Rotten Tomatoes", "94%"));
        ratings2.add(new Rating(6L,"Metacritic", "84/100"));
        details2.setRatings(ratings2);
        movie2.setDetails(details2);

        // Creating providers for movie2
        List<MovieProvider> providers2 = new ArrayList<>();
        providers2.add(new MovieProvider(4L,"Apple TV", MovieProvider.AccessType.RENTAL, movie2));
        providers2.add(new MovieProvider(5L,"Google Play Movies", MovieProvider.AccessType.PURCHASE, movie2));
        providers2.add(new MovieProvider(6L,"Amazon Video", MovieProvider.AccessType.SUBSCRIPTION, movie2));
        movie2.setProviders(providers2);

        user.getFavoriteMovies().add(movie1);
        user.getFavoriteMovies().add(movie2);
        userRepository.save(user);

        userToken = jwtUtility.generateToken(user.getUsername());

    }

    @Test
    void shouldReturnFavoriteMovies() throws Exception {
        // When&Then
        mockMvc.perform(get("/v1/movies")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tmdbId").value(14L))
                .andExpect(jsonPath("$[1].tmdbId").value(15L))
                .andExpect(jsonPath("$[0].details.Title").value("American Beauty"))
                .andExpect(jsonPath("$[1].details.Title").value("The Dark Knight"));
    }

    @Test
    void getMovies_shouldReturnForbidden_whenUserDoesNotHaveRoleUSER() throws Exception {
        // Given
        User admin = new User();
        admin.setFirst_name("Admin");
        admin.setLast_name("Admin");
        admin.setUsername("admins");
        admin.setEmail("admin@test.pl");
        admin.setPassword(passwordEncoder.encode("adminpassword"));
        admin.setStatus(User.UserStatus.ACTIVE);
        admin.setRoles(List.of(new Role(1L, "ADMIN")));
        userRepository.save(admin);

        String adminToken = jwtUtility.generateToken(admin.getUsername());

        // When & Then
        mockMvc.perform(get("/v1/movies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}