package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserDTO;
import com.kodilla.finalProject.errorHandling.EmailExistsException;
import com.kodilla.finalProject.errorHandling.RoleWithNameNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithIdNotFoundException;
import com.kodilla.finalProject.errorHandling.UsernameExistsException;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDto) throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {
        userService.registerUser(userDto, false);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    @Operation(description = "Delete user from database",
            summary = "Delete user")

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        boolean isDeleted = userService.deleteUserById(userId);
        if(isDeleted){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(description = "Update user in database",
            summary = "Update user")

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {
        try {
            userService.updateUser(userId, userDTO, false);
            return ResponseEntity.ok("Your data has been updated");
        } catch (UserWithIdNotFoundException | RoleWithNameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

