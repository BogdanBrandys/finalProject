package com.kodilla.finalProject.errorHandling;

public class EmailExistsException extends Exception {
    private String email;

    public EmailExistsException(String email) {
        super("Email " + email + " already exists.");
    }
}
