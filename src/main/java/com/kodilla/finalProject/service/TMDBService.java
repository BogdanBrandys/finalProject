package com.kodilla.finalProject.service;

import com.kodilla.finalProject.TMDB.client.TMDBClient;
import com.kodilla.finalProject.domain.MovieBasicDTO;
import com.kodilla.finalProject.domain.MovieProviderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TMDBService {

    private final TMDBClient tmdbClient;

    public List<MovieBasicDTO> searchMoviesInTMDB(String title) {
        return tmdbClient.searchMovie(title);

    }

    public List<MovieProviderDTO> searchProvidersInTMDB(Long tmdbId) {
        return tmdbClient.searchProviders(tmdbId);
    }

}
