package com.kodilla.finalProject.errorHandling;

public class RoleWithNameNotFoundException extends RuntimeException {
    public RoleWithNameNotFoundException(String roleName) {
        super("Role not found: " + roleName);
    }

}
