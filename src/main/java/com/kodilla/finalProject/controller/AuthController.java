package com.kodilla.finalProject.controller;


import com.kodilla.finalProject.domain.AuthResponse;
import com.kodilla.finalProject.domain.LoginRequest;
import com.kodilla.finalProject.errorHandling.InvalidCredentialsException;
import com.kodilla.finalProject.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/login")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Authenticate user and generate token",
            description = "Authenticates the user using their username and password. If the credentials are valid, it returns a token for future requests."
    )
    @PostMapping//POSTMAN
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) throws InvalidCredentialsException {
            String token = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));

    }
}
