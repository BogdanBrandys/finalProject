package com.kodilla.finalProject.errorHandling;

public class MovieExistsException extends Exception{
    private String title;
    private String year;
    public MovieExistsException(Long id) {
        super("Movie with id " + id + " already exists");
    }
}
