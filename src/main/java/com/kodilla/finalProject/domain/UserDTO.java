package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    @NotBlank(message = "firstname is required")
    private String first_name;
    @NotBlank(message = "lastname is required")
    private String last_name;
    @NotBlank(message = "email is required")
    @Email(message = "Wprowadź poprawy adres email") //show in frontend
    private String email;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków") //show in frontend
    private String password;
    private List<RoleDTO> roles;
}
