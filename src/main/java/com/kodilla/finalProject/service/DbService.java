package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.*;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DbService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserMovieRepository userMovieRepository;
    private final MovieDetailsRepository movieDetailsRepository;
    private final MovieProviderRepository movieProviderRepository;
    private final PhysicalVersionRepository physicalVersionRepository;
    private final CollectionMapper collectionMapper;
    private final OMDBService omdbService;
    private final TMDBService tmdbService;
    private final UserActionService userActionService;

    public Movie findOrCreateMovie(MovieBasicDTO movieBasicDTO){
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(movieBasicDTO.getId());
        Movie movie;

        if (existingMovie.isPresent()) {
            movie = existingMovie.get();  // get movie from database
            return movie;
        }

        MovieDetailsDTO movieDetailsDTO = omdbService.getMovieDetails(movieBasicDTO.getTitle(), movieBasicDTO.getRelease_date());

        MovieDetails movieDetails = collectionMapper.movieDetailsDTOToMovieDetails(movieDetailsDTO);

        Movie newMovie = new Movie();
        newMovie.setTmdbId(movieBasicDTO.getId());
        newMovie.setDetails(movieDetails);

        movieDetailsRepository.save(movieDetails);

        newMovie = movieRepository.save(newMovie);

        List<MovieProviderDTO> movieProviderDTOs = tmdbService.searchProvidersInTMDB(movieBasicDTO.getId());
        if (!movieProviderDTOs.isEmpty()) {
            List<MovieProvider> movieProviders = collectionMapper.movieProviderDTOListToMovieProviderList(movieProviderDTOs, newMovie);
            newMovie.setProviders(movieProviders);
        } else {
            newMovie.setProviders(Collections.emptyList());
        }

        movieProviderRepository.saveAll(newMovie.getProviders());
        return newMovie;
    }

    public void updatePhysicalVersion(Long movieId, PhysicalVersionDTO physicalVersionDto) throws MovieNotFoundException {
        // Pobranie nazwy aktualnie zalogowanego użytkownika
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));

        // Znalezienie filmu w ulubionych użytkownika na podstawie movieId
        Movie movie = currentUser.getFavoriteMovies().stream()
                .filter(favMovie -> favMovie.getId().equals(movieId))
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        // Sprawdzenie, czy fizyczna wersja już istnieje
        Optional<UserMovie> existingUserMovieOptional = userMovieRepository.findByUserAndMovie(currentUser, movie);

        if (existingUserMovieOptional.isPresent()) {
            // Jeśli fizyczna wersja już istnieje, aktualizujemy jej dane
            UserMovie existingUserMovie = existingUserMovieOptional.get();
            PhysicalVersion existingPhysicalVersion = existingUserMovie.getPhysicalVersion();

            if (existingPhysicalVersion != null) {
                // Aktualizacja fizycznej wersji, jeśli już istnieje
                existingPhysicalVersion.setDescription(physicalVersionDto.getDescription());
                existingPhysicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
                existingPhysicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
                existingPhysicalVersion.setDetails(physicalVersionDto.getDetails());

                // Zapisanie zaktualizowanej fizycznej wersji
                physicalVersionRepository.save(existingPhysicalVersion);
            } else {
                // Jeśli nie ma fizycznej wersji, tworzymy nową
                PhysicalVersion newPhysicalVersion = new PhysicalVersion();
                newPhysicalVersion.setDescription(physicalVersionDto.getDescription());
                newPhysicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
                newPhysicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
                newPhysicalVersion.setDetails(physicalVersionDto.getDetails());

                // Tworzenie nowego UserMovie z fizyczną wersją
                existingUserMovie.setPhysicalVersion(newPhysicalVersion);

                // Zapisanie zaktualizowanego UserMovie
                userMovieRepository.save(existingUserMovie);

                // Zapisanie nowej fizycznej wersji
                physicalVersionRepository.save(newPhysicalVersion);
            }
        } else {
            // Jeśli fizyczna wersja nie istnieje, tworzymy nową
            PhysicalVersion physicalVersion = new PhysicalVersion();
            physicalVersion.setDescription(physicalVersionDto.getDescription());
            physicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
            physicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
            physicalVersion.setDetails(physicalVersionDto.getDetails());

            // Tworzenie obiektu UserMovie i przypisanie fizycznej wersji
            UserMovie userMovie = new UserMovie();
            userMovie.setUser(currentUser);
            userMovie.setMovie(movie);
            userMovie.setPhysicalVersion(physicalVersion);

            // Zapisanie nowego UserMovie
            userMovieRepository.save(userMovie);

            // Zapisanie fizycznej wersji
            physicalVersionRepository.save(physicalVersion);
        }

        // Publikowanie zdarzenia związane z dodaniem/aktualizowaniem fizycznej wersji
        userActionService.publishUserActionEvent(currentUser, ActionType.ADD_PHYSICAL_VERSION);
    }

    public List<PhysicalVersionDTO> getPhysicalVersions() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));
        List<UserMovie> userMovies = userMovieRepository.findByUser_Id(currentUser.getId());
        userActionService.publishUserActionEvent(currentUser, ActionType.GET_ALL_PHYSICAL_VERSIONS);
        return userMovies.stream()
                .filter(userMovie -> userMovie.getPhysicalVersion() != null)
                .map(userMovie -> new PhysicalVersionDTO(
                        userMovie.getPhysicalVersion().getDescription(),
                        userMovie.getPhysicalVersion().getReleaseYear(),
                        userMovie.getPhysicalVersion().getSteelbook(),
                        userMovie.getPhysicalVersion().getDetails(),
                        userMovie.getMovie().getDetails().getTitle()
                ))
                .collect(Collectors.toList());
    }
}
