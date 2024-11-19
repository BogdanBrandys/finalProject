package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.errorHandling.InvalidCredentialsException;
import com.kodilla.finalProject.repository.UserRepository;
import com.kodilla.finalProject.security.JwtUtility;
import com.kodilla.finalProject.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final SecurityConfig securityConfig;

    public String authenticate(String username, String password) throws InvalidCredentialsException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && securityConfig.passwordEncoder().matches(password, user.get().getPassword())) {
            return jwtUtility.generateToken(username);
        } else {
            throw new InvalidCredentialsException();
        }
    }
}

