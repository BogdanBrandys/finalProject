package com.kodilla.finalProject.errorHandling;

public class InvalidCredentialsException extends Exception{
    public InvalidCredentialsException() {
        super("Invalid credentials: username or password.");
    }
}
