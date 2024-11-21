package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.errorHandling.InvalidCredentialsException;
import com.kodilla.finalProject.repository.UserRepository;
import com.kodilla.finalProject.security.JwtUtility;
import com.kodilla.finalProject.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock(lenient = true)
    private UserRepository userRepository;

    @Mock(lenient = true)
    private JwtUtility jwtUtility;

    @Mock(lenient = true)
    private SecurityConfig securityConfig;

    @InjectMocks
    private AuthService authService;

    private User user;
    private final String username = "steve_johnson";
    private final String password = "john1234";
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername(username);
        passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);

        when(jwtUtility.generateToken(username)).thenReturn("mocked.jwt.token");
    }

    @Test
    public void testAuthenticate_ValidCredentials() throws InvalidCredentialsException {
        //When
        String token = authService.authenticate(username, password);
        //Then
        assertEquals("mocked.jwt.token", token);
    }

    @Test
    public void testAuthenticate_InvalidUsername() {
        //When
        when(userRepository.findByUsername("invalid_user")).thenReturn(Optional.empty());
        //Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate("invalid_user", password);
        });
    }

    @Test
    public void testAuthenticate_InvalidPassword() {
        // Given
        User wrongPasswordUser = new User();
        wrongPasswordUser.setUsername(username);
        wrongPasswordUser.setPassword(passwordEncoder.encode("wrongPassword"));
        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(wrongPasswordUser));
        // Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate(username, password);
        });
    }
}