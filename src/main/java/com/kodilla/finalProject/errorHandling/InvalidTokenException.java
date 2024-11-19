package com.kodilla.finalProject.errorHandling;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super("Invalid token. ");
    }
}
