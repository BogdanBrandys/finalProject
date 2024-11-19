package com.kodilla.finalProject.errorHandling;

public class UserWithNameNotFoundException extends RuntimeException {
    public UserWithNameNotFoundException(String username) {
        super("User " + username + " not found.");
    }
}
