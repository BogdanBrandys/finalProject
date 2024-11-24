package com.kodilla.finalProject.service;

import com.kodilla.finalProject.OMDB.client.OMDBClient;
import com.kodilla.finalProject.domain.MovieDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OMDBService {

    private final OMDBClient omdbClient;

    public MovieDetailsDTO getMovieDetails(String movieTitle, String year) {
        try {
            return omdbClient.fetchMovieDetailsByTitleAndYear(movieTitle, year);
        } catch (RuntimeException e) {
            return new MovieDetailsDTO(movieTitle, null, year, null, null, Collections.emptyList());
        }
    }
}
