package com.kodilla.finalProject.errorHandling;

import lombok.Getter;

@Getter
public class UsernameExistsException extends Exception {
    private final String username;

    public UsernameExistsException(String username) {
        super("Username " + username + " already exists.");
        this.username = username;
    }
}