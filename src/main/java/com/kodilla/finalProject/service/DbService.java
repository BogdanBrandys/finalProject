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
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));

        Movie movie = currentUser.getFavoriteMovies().stream()
                .filter(favMovie -> favMovie.getId().equals(movieId))
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        Optional<UserMovie> existingUserMovieOptional = userMovieRepository.findByUserAndMovie(currentUser, movie);

        if (existingUserMovieOptional.isPresent()) {
            UserMovie existingUserMovie = existingUserMovieOptional.get();
            PhysicalVersion existingPhysicalVersion = existingUserMovie.getPhysicalVersion();

            if (existingPhysicalVersion != null) {
                existingPhysicalVersion.setDescription(physicalVersionDto.getDescription());
                existingPhysicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
                existingPhysicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
                existingPhysicalVersion.setDetails(physicalVersionDto.getDetails());

                physicalVersionRepository.save(existingPhysicalVersion);
            } else {
                PhysicalVersion newPhysicalVersion = new PhysicalVersion();
                newPhysicalVersion.setDescription(physicalVersionDto.getDescription());
                newPhysicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
                newPhysicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
                newPhysicalVersion.setDetails(physicalVersionDto.getDetails());

                existingUserMovie.setPhysicalVersion(newPhysicalVersion);

                userMovieRepository.save(existingUserMovie);

                physicalVersionRepository.save(newPhysicalVersion);
            }
        } else {
            PhysicalVersion physicalVersion = new PhysicalVersion();
            physicalVersion.setDescription(physicalVersionDto.getDescription());
            physicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
            physicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
            physicalVersion.setDetails(physicalVersionDto.getDetails());

            UserMovie userMovie = new UserMovie();
            userMovie.setUser(currentUser);
            userMovie.setMovie(movie);
            userMovie.setPhysicalVersion(physicalVersion);

            userMovieRepository.save(userMovie);

            physicalVersionRepository.save(physicalVersion);
        }

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
