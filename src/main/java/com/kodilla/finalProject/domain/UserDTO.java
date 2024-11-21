package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class UserDTO {
    private String first_name;
    private String last_name;
    private String email;
    private String username;
    private String password;
    private List<RoleDTO> roles;
}
