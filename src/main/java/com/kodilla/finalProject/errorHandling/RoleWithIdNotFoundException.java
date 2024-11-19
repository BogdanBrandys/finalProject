package com.kodilla.finalProject.errorHandling;

public class RoleWithIdNotFoundException extends RuntimeException {
    public RoleWithIdNotFoundException(Long id) {
        super("Role with id " + id + " not found");
    }
}
