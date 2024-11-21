package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserDTO;
import com.kodilla.finalProject.errorHandling.EmailExistsException;
import com.kodilla.finalProject.errorHandling.RoleWithNameNotFoundException;
import com.kodilla.finalProject.errorHandling.UsernameExistsException;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
            description = "Get all user from database",
            summary = "Get users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            description = "Get user identified by its ID",
            summary = "Get user"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            description = "Create new user in database",
            summary = "Create user"
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUserByAdmin(@RequestBody UserDTO userDto) throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {
        userService.registerUser(userDto, true);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    @Operation(
            description = "Update user identified by its ID.",
            summary = "Update user"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {
        User updatedUser = userService.updateUser(id, userDTO, true);
        return ResponseEntity.ok(userMapper.mapUserToUserDto(updatedUser));
    }

    @Operation(
            description = "Deletes user identified by its ID.",
            summary = "Delete user"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        boolean isDeleted = userService.deleteUserById(id);
        if(isDeleted){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
