package com.kodilla.finalProject.errorHandling;

public class TokenMissingException extends RuntimeException {
    public TokenMissingException() {
        super("Token is missing or malformed.");
    }
}
