package com.kodilla.finalProject.errorHandling;

public class TokenFormatException extends RuntimeException {
    public TokenFormatException() {
        super("Invalid token format.");
    }
}