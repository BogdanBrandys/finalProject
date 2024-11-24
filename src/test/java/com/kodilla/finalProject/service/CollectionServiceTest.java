package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.MovieInUsersListException;
import com.kodilla.finalProject.errorHandling.MovieNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithIdNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollectionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActionService userActionService;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private DbService dbService;

    @InjectMocks
    private CollectionService collectionService;

    String username;

    @BeforeEach
    public void setUp() {
        username = "testUser";
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(username); //when is necessary
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication); //when is necessary
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void getMovies_shouldReturnMoviesFromFavourites() {
        // Given
        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Movie Title");
        movieDetails.setGenre("Action");
        movieDetails.setYear("2020");
        movieDetails.setDirector("Director");
        movieDetails.setPlot("Plot description");

        Movie movie = new Movie();
        movie.setTmdbId(12345L);
        movie.setDetails(movieDetails);

        user.getFavoriteMovies().add(movie);

        PhysicalVersionDTO physicalVersionDTO = new PhysicalVersionDTO();
        physicalVersionDTO.setMovieTitle("Movie Title");

        List<PhysicalVersionDTO> physicalVersions = List.of(physicalVersionDTO);

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(dbService.getPhysicalVersions()).thenReturn(physicalVersions);

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setMovie_id(movie.getId());
        movieDTO.setTmdbId(movie.getTmdbId());

        when(collectionMapper.mapToMovieDTO(movie)).thenReturn(movieDTO);

        // Then
        List<MovieDTO> result = collectionService.getAllMoviesFromFavourites();

        // Sprawdzenie rezultatów
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movie.getId(), result.get(0).getMovie_id());
        assertEquals(movie.getTmdbId(), result.get(0).getTmdbId());
        assertNotNull(result.get(0).getPhysicalVersion());
        verify(userActionService).publishUserActionEvent(user, ActionType.VIEW_FAVORITES);
    }

    @Test
    public void getMovies_shouldThrowUserWithNameNotFoundExceptionWhenUserNotFound() {
        // Given

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Then
        assertThrows(UserWithNameNotFoundException.class, () -> collectionService.getAllMoviesFromFavourites());
    }

    @Test
    public void getOneMovie_shouldReturnMovieFromFavouritesWithPhysicalVersion() throws MovieNotFoundException {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Movie Title");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTmdbId(123L);
        movie.setDetails(movieDetails);

        user.getFavoriteMovies().add(movie);

        PhysicalVersionDTO physicalVersionDTO = new PhysicalVersionDTO();
        physicalVersionDTO.setMovieTitle("Movie Title");

        List<PhysicalVersionDTO> physicalVersions = List.of(physicalVersionDTO);

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(dbService.getPhysicalVersions()).thenReturn(physicalVersions);

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setMovie_id(movieId);
        when(collectionMapper.mapToMovieDTO(movie)).thenReturn(movieDTO);

        MovieDTO result = collectionService.getMovieFromFavourites(movieId);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getMovie_id());
        assertNotNull(result.getPhysicalVersion());
        assertEquals(physicalVersionDTO, result.getPhysicalVersion());
        verify(userActionService).publishUserActionEvent(user, ActionType.VIEW_FAVORITES);
    }

    @Test
    public void getOneMovie_shouldThrowMovieNotFoundExceptionWhenMovieNotInFavourites() {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Then
        assertThrows(MovieNotFoundException.class, () -> collectionService.getMovieFromFavourites(movieId));
    }

    @Test
    public void getOneMovie_shouldThrowUserWithNameNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        Long movieId = 1L;

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Then
        assertThrows(UserWithNameNotFoundException.class, () -> collectionService.getMovieFromFavourites(movieId));
    }

    @Test
    public void addMovieToUserFavorites_shouldAddMovieToFavorites() {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Movie Title");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTmdbId(123L);
        movie.setDetails(movieDetails);

        // When
        doNothing().when(userActionService).publishUserActionEvent(any(User.class), any(ActionType.class));

        collectionService.addMovieToUserFavorites(movie, user);

        // Then
        assertTrue(user.getFavoriteMovies().contains(movie));
        verify(userRepository).save(user);
        verify(userActionService).publishUserActionEvent(user, ActionType.ADD_TO_FAVORITES);
    }

    @Test
    public void addMovieToUserFavorites_shouldThrowMovieInUsersListException() {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Movie Title");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTmdbId(123L);
        movie.setDetails(movieDetails);

        user.setFavoriteMovies(new ArrayList<>());
        user.getFavoriteMovies().add(movie);

        // Then
        assertThrows(MovieInUsersListException.class, () -> {
            collectionService.addMovieToUserFavorites(movie, user);
        });
        verify(userRepository, never()).save(user);
    }

    @Test
    public void deleteMovieFromFavourites_shouldRemoveMovie() throws MovieNotFoundException, UserWithNameNotFoundException {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Movie Title");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTmdbId(123L);
        movie.setDetails(movieDetails);

        user.getFavoriteMovies().add(movie);

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doNothing().when(userActionService).publishUserActionEvent(any(User.class), any(ActionType.class));

        boolean result = collectionService.deleteMovieFromFavourites(movieId);

        // Then
        assertTrue(result);
        assertFalse(user.getFavoriteMovies().contains(movie));
        verify(userRepository).save(user);
        verify(userActionService).publishUserActionEvent(user, ActionType.REMOVE_FROM_FAVORITES);
    }

    @Test
    public void deleteMovieFromFavourites_shouldThrowMovieNotFoundException() {
        // Given
        Long movieId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Then
        assertThrows(MovieNotFoundException.class, () -> {
            collectionService.deleteMovieFromFavourites(movieId);
        });
        verify(userRepository, never()).save(user);
    }

    @Test
    public void getCollectionStats_shouldReturnEmptyStatsWhenNoMovies() throws UserWithIdNotFoundException {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setFavoriteMovies(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MovieCollectionStatsDTO result = collectionService.getCollectionStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalMovies());
        assertEquals("N/A", result.getMostCommonGenre());
        assertEquals("N/A", result.getOldestMovieYear());
        assertEquals("N/A", result.getNewestMovieYear());
    }
    @Test
    public void getCollectionStats_shouldReturnCorrectStatsWhenMoviesExist() throws UserWithIdNotFoundException {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        MovieDetails movieDetails1 = new MovieDetails();
        movieDetails1.setTitle("Movie 1");
        movieDetails1.setGenre("Action, Adventure");
        movieDetails1.setYear("2000");

        MovieDetails movieDetails2 = new MovieDetails();
        movieDetails2.setTitle("Movie 2");
        movieDetails2.setGenre("Action, Drama");
        movieDetails2.setYear("2020");

        Movie movie1 = new Movie();
        movie1.setDetails(movieDetails1);
        movie1.setId(1L);

        Movie movie2 = new Movie();
        movie2.setDetails(movieDetails2);
        movie2.setId(2L);

        user.setFavoriteMovies(List.of(movie1, movie2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MovieCollectionStatsDTO result = collectionService.getCollectionStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalMovies());
        assertEquals("Action", result.getMostCommonGenre());
        assertEquals("2000", result.getOldestMovieYear());
        assertEquals("2020", result.getNewestMovieYear());
        verify(userActionService).publishUserActionEvent(user, ActionType.GET_COLLECTION_STATS);
    }
    @Test
    public void getCollectionStats_shouldHandleMissingGenreOrYear() throws UserWithIdNotFoundException {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        MovieDetails movieDetails1 = new MovieDetails();
        movieDetails1.setTitle("Movie 1");
        movieDetails1.setGenre("Action");
        movieDetails1.setYear("1999");

        MovieDetails movieDetails2 = new MovieDetails();
        movieDetails2.setTitle("Movie 2");
        movieDetails2.setGenre(null);
        movieDetails2.setYear("2022");

        Movie movie1 = new Movie();
        movie1.setDetails(movieDetails1);
        movie1.setId(1L);

        Movie movie2 = new Movie();
        movie2.setDetails(movieDetails2);
        movie2.setId(2L);

        user.setFavoriteMovies(List.of(movie1, movie2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MovieCollectionStatsDTO result = collectionService.getCollectionStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalMovies());
        assertEquals("Action", result.getMostCommonGenre());
        assertEquals("1999", result.getOldestMovieYear());
        assertEquals("2022", result.getNewestMovieYear());
        verify(userActionService).publishUserActionEvent(user, ActionType.GET_COLLECTION_STATS);
    }
    @Test
    public void getCollectionStats_shouldReturnStatsForOneMovie() throws UserWithIdNotFoundException {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Single Movie");
        movieDetails.setGenre("Drama");
        movieDetails.setYear("2015");

        Movie movie = new Movie();
        movie.setDetails(movieDetails);
        movie.setId(1L);

        user.setFavoriteMovies(List.of(movie));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MovieCollectionStatsDTO result = collectionService.getCollectionStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalMovies());
        assertEquals("Drama", result.getMostCommonGenre());
        assertEquals("2015", result.getOldestMovieYear());
        assertEquals("2015", result.getNewestMovieYear());
        verify(userActionService).publishUserActionEvent(user, ActionType.GET_COLLECTION_STATS);
    }
}