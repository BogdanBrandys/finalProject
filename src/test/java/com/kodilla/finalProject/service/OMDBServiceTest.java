package com.kodilla.finalProject.service;

import com.kodilla.finalProject.OMDB.client.OMDBClient;
import com.kodilla.finalProject.domain.MovieDetailsDTO;
import com.kodilla.finalProject.domain.RatingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OMDBServiceTest {

    @Mock
    private OMDBClient omdbClient;

    @InjectMocks
    private OMDBService omdbService;

    private MovieDetailsDTO movieDetailsDTO;

    @BeforeEach
    void setUp() {

        movieDetailsDTO = new MovieDetailsDTO();
        movieDetailsDTO.setTitle("Test Movie");
        movieDetailsDTO.setYear("2024");
        movieDetailsDTO.setRatings(Collections.singletonList(new RatingDTO("IMDB", "8.5")));
    }

    @Test
    void testGetMovieDetails_ShouldReturnMovieDetails() {
        // Given
        String movieTitle = "Test Movie";
        String year = "2024";

        when(omdbClient.fetchMovieDetailsByTitleAndYear(movieTitle, year)).thenReturn(movieDetailsDTO);

        // When
        MovieDetailsDTO result = omdbService.getMovieDetails(movieTitle, year);

        // Then
        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals("2024", result.getYear());
        assertFalse(result.getRatings().isEmpty());
        assertEquals("IMDB", result.getRatings().get(0).getSource());
        assertEquals("8.5", result.getRatings().get(0).getValue());

        verify(omdbClient, times(1)).fetchMovieDetailsByTitleAndYear(movieTitle, year);
    }

    @Test
    void testGetMovieDetails_ShouldReturnEmptyWhenNoMovieFound() {
        // Given
        String movieTitle = "NonExistent Movie";
        String year = "2024";

        //When
        when(omdbClient.fetchMovieDetailsByTitleAndYear(movieTitle, year)).thenReturn(new MovieDetailsDTO(movieTitle, null, year, null, null, Collections.emptyList()));
        MovieDetailsDTO result = omdbService.getMovieDetails(movieTitle, year);

        // Then
        assertNotNull(result);
        assertEquals("NonExistent Movie", result.getTitle());
        assertEquals("2024", result.getYear());
        assertTrue(result.getRatings().isEmpty());
        verify(omdbClient, times(1)).fetchMovieDetailsByTitleAndYear(movieTitle, year);
    }

    @Test
    void testGetMovieDetails_ShouldHandleException() {
        // Given
        String movieTitle = "Test Movie";
        String year = "2024";

        // Symulacja wyjątku w metodzie omdbClient.fetchMovieDetailsByTitleAndYear
        when(omdbClient.fetchMovieDetailsByTitleAndYear(movieTitle, year)).thenThrow(new RuntimeException("Error fetching data"));

        // When
        MovieDetailsDTO result = omdbService.getMovieDetails(movieTitle, year);

        // Then
        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals("2024", result.getYear());
        assertTrue(result.getRatings().isEmpty());

        verify(omdbClient, times(1)).fetchMovieDetailsByTitleAndYear(movieTitle, year);
    }
}