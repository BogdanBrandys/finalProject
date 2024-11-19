package com.kodilla.finalProject.errorHandling;

public class MovieNotFoundException extends Exception{
    private int id;
    public MovieNotFoundException(Long id) {
        super("Movie with given ID: " + id + " was not found.");
    }
}
