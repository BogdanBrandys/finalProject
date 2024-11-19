package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.MovieExistsException;
import com.kodilla.finalProject.errorHandling.MovieNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.UserRepository;
import com.kodilla.finalProject.service.DbService;
import com.kodilla.finalProject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Managing movies")
public class CollectionController {
    private final DbService dbService;
    private final UserService userService;
    private final CollectionMapper movieMapper;
    private final UserRepository userRepository;


    @Operation(
            description = "Get all movies from user's list",
            summary = "Get movies"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MovieDTO>> getMovies() {
        List<Movie> movies = dbService.getAllMoviesFromFavourites();
        return ResponseEntity.ok(movieMapper.mapToMovieList(movies));
    }

    @Operation(
            description = "Get selected movie from user's list",
            summary = "Get selected movie"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value ="/user/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> getMovie(@PathVariable Long movieId) throws MovieNotFoundException {
        return ResponseEntity.ok(movieMapper.mapToMovieDTO(dbService.getMovieFromFavourites(movieId)));
    }

    @Operation(
            description = "Delete selected movie from user's list",
            summary = "Delete selected movie"
    )
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value ="/user/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) throws MovieNotFoundException {
        boolean isDeleted = dbService.deleteMovieFromFavourites(movieId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            description = "Create a movie if it's not already in the database and add it to the user's list of favorite videos",
            summary = "Create a movie and add to user's list"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/user/add", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieDTO> addMovie(@RequestBody MovieBasicDTO movieBasicDTO) throws MovieExistsException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));

        Movie movie = dbService.findOrCreateMovie(movieBasicDTO);

        userService.addMovieToUserFavorites(movie, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(movieMapper.mapToMovieDTO(movie));
    }

    @Operation(
            description = "Get collection's statistics with: movies' count, most common genre oldest and newest movie in collection",
            summary = "Get collection's statistics"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/{userId}/stats", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieCollectionStatsDTO> getUserMovieStats(@PathVariable Long userId) {
        MovieCollectionStatsDTO stats = userService.getCollectionStats(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(
            description = "Add information about physical version to movie in your collection",
            summary = "Add information about physical version"
    )
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/physical")
    public ResponseEntity<String> updatePhysicalVersion(
            @PathVariable Long id,
            @RequestBody PhysicalVersionDTO physicalVersionDto) throws MovieNotFoundException{
        dbService.updatePhysicalVersion(id, physicalVersionDto);
        return ResponseEntity.ok("Physical version details updated successfully.");
    }

    @Operation(
            description = "Get information about physical versions in user's collection",
            summary = "Get information about physical versions"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/physical-versions")
    public ResponseEntity<List<PhysicalVersionDTO>> getPhysicalVersions() {
        List<PhysicalVersionDTO> physicalVersions = dbService.getPhysicalVersions();
        return ResponseEntity.ok(physicalVersions);
    }
}



