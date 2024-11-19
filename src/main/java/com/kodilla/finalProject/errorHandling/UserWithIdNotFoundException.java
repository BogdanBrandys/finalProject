package com.kodilla.finalProject.errorHandling;

public class UserWithIdNotFoundException extends RuntimeException {
    public UserWithIdNotFoundException(Long id) {
        super("User " + id + " not found.");
    }
}
