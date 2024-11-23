package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.MovieBasicDTO;
import com.kodilla.finalProject.domain.MovieProviderDTO;
import com.kodilla.finalProject.service.TMDBService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/tmdb")
@RequiredArgsConstructor
public class TMDBController {

    private final TMDBService tmdbService;

    @Operation(description = "Searches for a movie in the TMDB database by its name. Retrieves the tmdbId, title and year of the movie",
            summary = "Searches for a movie and retrieve Basic Data"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value ="/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MovieBasicDTO>> searchMovie(@RequestParam String title) {
        List<MovieBasicDTO> movies = tmdbService.searchMoviesInTMDB(title);
        return ResponseEntity.ok(movies);
    }

    @Operation(description = "Technical endpoint. Searches movie providers in PL region by tmdbId",
            summary = "Searches movie providers"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MovieProviderDTO>> searchProviders(@RequestParam Long id) {
        List<MovieProviderDTO> providers = tmdbService.searchProvidersInTMDB(id);
        return ResponseEntity.ok(providers);
    }

}
