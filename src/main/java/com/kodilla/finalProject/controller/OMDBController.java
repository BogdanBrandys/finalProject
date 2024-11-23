package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.MovieDetailsDTO;
import com.kodilla.finalProject.service.OMDBService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class OMDBController {

    private final OMDBService omdbService;

    @Operation(description = "Technical endpoint. Searches movie details by its title and year",
            summary = "Searches movie details"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/omdb/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDetailsDTO> getMovieDetails(@RequestParam("t") String title,
                                                           @RequestParam("y") String year) {
        MovieDetailsDTO movieDetails = omdbService.getMovieDetails(title,year);
        return ResponseEntity.ok(movieDetails);
    }
}
