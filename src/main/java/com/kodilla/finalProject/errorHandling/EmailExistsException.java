package com.kodilla.finalProject.errorHandling;

import lombok.Getter;

@Getter
public class EmailExistsException extends Exception {
    private final String email;
    public EmailExistsException(String email) {
        super("Email " + email + " already exists.");
        this.email = email;
    }
}
