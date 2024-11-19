package com.kodilla.finalProject.errorHandling;

public class MovieInUsersListException extends RuntimeException {
    public MovieInUsersListException(String title) {
        super("Movie " + title + " is in the user's list.");
    }
}
