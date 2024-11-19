package com.kodilla.finalProject.event;

public enum ActionType {
    LOGIN,
    LOGOUT,
    ADD_MOVIE,
    DELETE_MOVIE,
    UPDATE_MOVIE,
    SEARCH_MOVIE,
    ADD_TO_FAVORITES,
    REMOVE_FROM_FAVORITES,
    UPDATE_PROFILE,
    VIEW_PROFILE,
    OTHER;

    public String getDescription() {
        switch (this) {
            case LOGIN: return "User logged in";
            case LOGOUT: return "User logged out";
            case ADD_MOVIE: return "User added a movie";
            case DELETE_MOVIE: return "User deleted a movie";
            case UPDATE_MOVIE: return "User updated a movie";
            case SEARCH_MOVIE: return "User searched for movies";
            case ADD_TO_FAVORITES: return "User added a movie to favorites";
            case REMOVE_FROM_FAVORITES: return "User removed a movie from favorites";
            case UPDATE_PROFILE: return "User updated their profile";
            case VIEW_PROFILE: return "User viewed their profile";
            case OTHER: return "Other action";
            default: return "Unknown action";
        }
    }
}
