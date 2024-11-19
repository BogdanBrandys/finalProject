package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserDTO;
import com.kodilla.finalProject.errorHandling.EmailExistsException;
import com.kodilla.finalProject.errorHandling.UsernameExistsException;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/users")
@Tag(name = "Users", description = "Managing users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(description = "Registers a new user with the provided username, email, and other details",
            summary = "Create a new user"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDto) throws UsernameExistsException, EmailExistsException {
        User user = userMapper.mapUserDtoToUser(userDto);

        User savedUser = userService.registerUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body((userMapper.mapUserToUserDto(savedUser)));
    }
}

