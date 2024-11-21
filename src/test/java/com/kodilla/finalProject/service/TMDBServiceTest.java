package com.kodilla.finalProject.service;

import com.kodilla.finalProject.TMDB.client.TMDBClient;
import com.kodilla.finalProject.domain.MovieBasicDTO;
import com.kodilla.finalProject.domain.MovieProvider;
import com.kodilla.finalProject.domain.MovieProviderDTO;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TMDBServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TMDBClient tmdbClient;

    @Mock
    private UserActionService userActionService;

    @InjectMocks
    private TMDBService tmdbService;

    @Test
    void testSearchMoviesInTMDB() {
        // Given
        String title = "Inception";
        String username = "testuser";

        //Mock Security
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        //Mock User
        User mockUser = mock(User.class);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Mock event
        UserActionService mockUserActionService = mock(UserActionService.class);
        mockUserActionService.publishUserActionEvent(mockUser, ActionType.SEARCH_MOVIE);

        // Mock service response
        List<MovieBasicDTO> movieList = List.of(new MovieBasicDTO(1L, "Inception", "2010"));
        when(tmdbService.searchMoviesInTMDB(title)).thenReturn(movieList);

        // When
        List<MovieBasicDTO> result = tmdbService.searchMoviesInTMDB(title);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());

        verify(tmdbClient).searchMovie(title);

        SecurityContextHolder.clearContext();
    }
    @Test
    void testSearchMoviesInTMDB_UserNotFound() {
        // Given
        String title = "Inception";
        String username = "testuser";

        // Mock security
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock user
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        UserWithNameNotFoundException exception = assertThrows(UserWithNameNotFoundException.class,
                () -> tmdbService.searchMoviesInTMDB(title));

        // Then
        assertEquals("User " + username + " not found.", exception.getMessage());

        SecurityContextHolder.clearContext();
    }

    @Test
    void testSearchProvidersInTMDB() {
        // Given
        Long tmdbId = 123L;
        List<MovieProviderDTO> providerList = List.of(new MovieProviderDTO("Netflix", MovieProvider.AccessType.SUBSCRIPTION));

        // Mockowanie odpowiedzi z tmdbClient
        when(tmdbClient.searchProviders(tmdbId)).thenReturn(providerList);

        // When: Wywołanie metody z serwisu
        List<MovieProviderDTO> result = tmdbService.searchProvidersInTMDB(tmdbId);

        // Then: Sprawdzenie wyników
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Netflix", result.get(0).getProvider_name());

        // Verify the interaction with tmdbClient
        verify(tmdbClient).searchProviders(tmdbId);
    }

    @Test
    void testSearchProvidersInTMDB_NoProviders() {
        // Given
        Long tmdbId = 123L;

        // Mockowanie pustej odpowiedzi z tmdbClient
        when(tmdbClient.searchProviders(tmdbId)).thenReturn(List.of());

        // When: Wywołanie metody z serwisu
        List<MovieProviderDTO> result = tmdbService.searchProvidersInTMDB(tmdbId);

        // Then: Sprawdzenie, czy wynik jest pusty
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify the interaction with tmdbClient
        verify(tmdbClient).searchProviders(tmdbId);
    }
}