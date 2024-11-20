package com.kodilla.finalProject.event;

public enum ActionType {
    REGISTER_USER,
    LOGIN,
    VIEW_FAVORITES,
    ADD_TO_FAVORITES,
    REMOVE_FROM_FAVORITES,
    ADD_PHYSICAL_VERSION,
    GET_ALL_PHYSICAL_VERSIONS,
    SEARCH_MOVIE,
    UPDATE_PROFILE,
    DELETE_PROFILE,
    GET_COLLECTION_STATS;

    public String getDescription() {
        switch (this) {
            case REGISTER_USER: return "Register new user";
            case LOGIN: return "User logged in";
            case VIEW_FAVORITES: return "User viewed favourites movies";
            case ADD_TO_FAVORITES: return "User added a movie to favorites";
            case REMOVE_FROM_FAVORITES: return "User removed a movie from favorites";
            case ADD_PHYSICAL_VERSION: return "User added physical version to his movie";
            case GET_ALL_PHYSICAL_VERSIONS: return "User viewed physical versions of his movies";
            case SEARCH_MOVIE: return "User searched for movies in TMDB";
            case UPDATE_PROFILE: return "User updated his profile";
            case DELETE_PROFILE: return "User deleted his profile";
            case GET_COLLECTION_STATS: return "User viewed collection stats";
            default: return "Unknown action";
        }
    }
}
