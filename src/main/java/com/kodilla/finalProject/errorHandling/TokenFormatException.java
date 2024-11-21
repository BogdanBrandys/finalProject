package com.kodilla.finalProject.errorHandling;

public class TokenFormatException extends RuntimeException {
    public TokenFormatException(String message) {
        super(message);
    }
}