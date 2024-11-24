package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.MovieNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private UserMovieRepository userMovieRepository;
    @Mock
    private MovieDetailsRepository movieDetailsRepository;
    @Mock
    private MovieProviderRepository movieProviderRepository;
    @Mock
    private PhysicalVersionRepository physicalVersionRepository;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private OMDBService omdbService;
    @Mock
    private TMDBService tmdbService;
    @Mock
    private UserActionService userActionService;

    @InjectMocks
    private DbService dbService;

    String username;

    @BeforeEach
    void setUp() {
        username = "testUser";
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(username); //when is necessary
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication); //when is necessary
        SecurityContextHolder.setContext(securityContext);
    }
    @Test
    void creatingMovie_shouldReturnExistingMovie() {
        // Given
        Movie existingMovie = new Movie();
        existingMovie.setTmdbId(123L);
        MovieBasicDTO movieBasicDTO = new MovieBasicDTO(123L, "Example Title", "2024");

        when(movieRepository.findByTmdbId(123L)).thenReturn(Optional.of(existingMovie));

        // When
        Movie result = dbService.findOrCreateMovie(movieBasicDTO);

        // Then
        assertEquals(existingMovie, result);
        verify(movieRepository).findByTmdbId(123L);
        verifyNoMoreInteractions(movieRepository); //dbService return existing movie
    }

    @Test
    void creatingMovie_shouldCreateNewMovieWithProviders() {
        // Given
        MovieBasicDTO movieBasicDTO = new MovieBasicDTO(123L, "Example Title", "2024");
        MovieDetailsDTO movieDetailsDTO = new MovieDetailsDTO("Example Title", "Genre", "2024", "Director", "Plot", new ArrayList<>());
        MovieDetails movieDetails = new MovieDetails();
        List<MovieProviderDTO> movieProviderDTOs = List.of(new MovieProviderDTO("Provider1", MovieProvider.AccessType.SUBSCRIPTION));
        List<MovieProvider> movieProviders = List.of(new MovieProvider());

        when(movieRepository.findByTmdbId(123L)).thenReturn(Optional.empty());
        when(omdbService.getMovieDetails("Example Title", "2024")).thenReturn(movieDetailsDTO);
        when(collectionMapper.movieDetailsDTOToMovieDetails(movieDetailsDTO)).thenReturn(movieDetails);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tmdbService.searchProvidersInTMDB(123L)).thenReturn(movieProviderDTOs);
        when(collectionMapper.movieProviderDTOListToMovieProviderList(anyList(), any(Movie.class))).thenReturn(movieProviders);

        // When
        Movie result = dbService.findOrCreateMovie(movieBasicDTO);

        // Then
        assertEquals(123L, result.getTmdbId());
        assertEquals(movieDetails, result.getDetails());
        assertEquals(movieProviders, result.getProviders());

        verify(movieRepository).findByTmdbId(123L);
        verify(omdbService).getMovieDetails("Example Title", "2024");
        verify(movieDetailsRepository).save(movieDetails);
        verify(movieRepository).save(any(Movie.class));
        verify(tmdbService).searchProvidersInTMDB(123L);
        verify(movieProviderRepository).saveAll(movieProviders);
    }

    @Test
    void creatingMovie_shouldCreateNewMovieWithoutProviders() {
        // Given
        MovieBasicDTO movieBasicDTO = new MovieBasicDTO(123L, "Example Title", "2024");
        MovieDetailsDTO movieDetailsDTO = new MovieDetailsDTO("Example Title", "Genre", "2024", "Director", "Plot", new ArrayList<>());
        MovieDetails movieDetails = new MovieDetails();

        when(movieRepository.findByTmdbId(123L)).thenReturn(Optional.empty());
        when(omdbService.getMovieDetails("Example Title", "2024")).thenReturn(movieDetailsDTO);
        when(collectionMapper.movieDetailsDTOToMovieDetails(movieDetailsDTO)).thenReturn(movieDetails);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tmdbService.searchProvidersInTMDB(123L)).thenReturn(Collections.emptyList());

        // When
        Movie result = dbService.findOrCreateMovie(movieBasicDTO);

        // Then
        assertEquals(123L, result.getTmdbId());
        assertEquals(movieDetails, result.getDetails());
        assertTrue(result.getProviders().isEmpty());

        verify(movieRepository).findByTmdbId(123L);
        verify(omdbService).getMovieDetails("Example Title", "2024");
        verify(movieDetailsRepository).save(movieDetails);
        verify(movieRepository).save(any(Movie.class));
        verify(tmdbService).searchProvidersInTMDB(123L);
    }

    @Test
    void creatingMovie_shouldThrowExceptionWhenOmdbServiceFails() {
        // Given
        MovieBasicDTO movieBasicDTO = new MovieBasicDTO(123L, "Example Title", "2024");

        when(movieRepository.findByTmdbId(123L)).thenReturn(Optional.empty());
        when(omdbService.getMovieDetails("Example Title", "2024")).thenThrow(new RuntimeException("OMDB Service Error"));

        // When / Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dbService.findOrCreateMovie(movieBasicDTO));
        assertEquals("OMDB Service Error", exception.getMessage());

        verify(movieRepository).findByTmdbId(123L);
        verify(omdbService).getMovieDetails("Example Title", "2024");
        verifyNoInteractions(collectionMapper, movieDetailsRepository, movieProviderRepository);
    }
    @Test
    void updatePhysicalVersion_shouldUpdateWhenMovieExists() throws MovieNotFoundException {
        // Given
        Long movieId = 1L;
        PhysicalVersionDTO physicalVersionDto = new PhysicalVersionDTO("Description", 2024, true, "Details", "Test Title");

        User user = new User();
        user.setUsername(username);

        Movie movie = new Movie();
        movie.setId(movieId);

        user.setFavoriteMovies(List.of(movie));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(username);

        PhysicalVersion physicalVersion = new PhysicalVersion();
        when(physicalVersionRepository.save(any(PhysicalVersion.class))).thenReturn(physicalVersion);

        UserMovie userMovie = new UserMovie();
        when(userMovieRepository.save(any(UserMovie.class))).thenReturn(userMovie);

        // When
        dbService.updatePhysicalVersion(movieId, physicalVersionDto);

        // Then
        verify(userRepository).findByUsername(username);
        verify(physicalVersionRepository).save(any(PhysicalVersion.class));
        verify(userMovieRepository).save(any(UserMovie.class));
        verify(userActionService).publishUserActionEvent(user, ActionType.ADD_PHYSICAL_VERSION);
    }

    @Test
    void updatePhysicalVersion_shouldThrowMovieNotFoundExceptionWhenMovieNotInFavorites() {
        // Given
        Long movieId = 1L;
        PhysicalVersionDTO physicalVersionDto = new PhysicalVersionDTO("Description", 2024, true, "Details", "Test Title");

        User user = new User();
        user.setUsername(username);
        user.setFavoriteMovies(List.of()); // No favorite movies

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(username);

        // When & Then
        assertThrows(MovieNotFoundException.class, () -> dbService.updatePhysicalVersion(movieId, physicalVersionDto));

        verify(userRepository).findByUsername(username);
        verifyNoInteractions(physicalVersionRepository);
        verifyNoInteractions(userMovieRepository);
        verifyNoInteractions(userActionService);
    }

    @Test
    void updatePhysicalVersion_shouldThrowUserWithNameNotFoundExceptionWhenUserNotFound() {
        // Given
        Long movieId = 1L;
        PhysicalVersionDTO physicalVersionDto = new PhysicalVersionDTO("Description", 2024, true, "Details","Test Title");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(username);

        // When & Then
        assertThrows(UserWithNameNotFoundException.class, () -> dbService.updatePhysicalVersion(movieId, physicalVersionDto));

        verify(userRepository).findByUsername(username);
        verifyNoInteractions(physicalVersionRepository);
        verifyNoInteractions(userMovieRepository);
        verifyNoInteractions(userActionService);
    }
    @Test
    void getPhysicalVersion_shouldReturnListOfPhysicalVersions() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        PhysicalVersion physicalVersion = new PhysicalVersion();
        physicalVersion.setDescription("Description");
        physicalVersion.setReleaseYear(2024);
        physicalVersion.setSteelbook(true);
        physicalVersion.setDetails("Details");

        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setTitle("Example Movie");

        Movie movie = new Movie();
        movie.setDetails(movieDetails);

        UserMovie userMovie = new UserMovie();
        userMovie.setPhysicalVersion(physicalVersion);
        userMovie.setMovie(movie);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(username);
        when(userMovieRepository.findByUser_Id(user.getId())).thenReturn(List.of(userMovie));

        // When
        List<PhysicalVersionDTO> result = dbService.getPhysicalVersions();

        // Then
        assertEquals(1, result.size());
        PhysicalVersionDTO dto = result.get(0);
        assertEquals("Description", dto.getDescription());
        assertEquals(2024, dto.getReleaseYear());
        assertTrue(dto.getSteelbook());
        assertEquals("Details", dto.getDetails());
        assertEquals("Example Movie", dto.getMovieTitle());

        verify(userRepository).findByUsername(username);
        verify(userMovieRepository).findByUser_Id(user.getId());
        verify(userActionService).publishUserActionEvent(user, ActionType.GET_ALL_PHYSICAL_VERSIONS);
    }

    @Test
    void getPhysicalVersion_shouldThrowUserWithNameNotFoundExceptionWhenUserNotFoundWhileGettingPhysicalVersion() {
        // Given

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(username);

        // When & Then
        assertThrows(UserWithNameNotFoundException.class, () -> dbService.getPhysicalVersions());

        verify(userRepository).findByUsername(username);
        verifyNoInteractions(userMovieRepository);
        verifyNoInteractions(userActionService);
    }
}