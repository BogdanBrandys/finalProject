package com.kodilla.finalProject.errorHandling;

import lombok.Getter;

@Getter
public class RoleWithNameNotFoundException extends RuntimeException {
    private final String roleName;
    public RoleWithNameNotFoundException(String roleName) {
        super("Role not found: " + roleName);
        this.roleName = roleName;
    }

}
