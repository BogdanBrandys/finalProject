package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.*;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.CollectionRepository;
import com.kodilla.finalProject.repository.UserMovieRepository;
import com.kodilla.finalProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DbService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final UserMovieRepository movieRepository;
    private final CollectionMapper collectionMapper;
    private final OMDBService omdbService;
    private final TMDBService tmdbService;
    private final UserActionService userActionService;

    public Movie findOrCreateMovie(MovieBasicDTO movieBasicDTO) throws MovieExistsException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));

        Optional<Movie> existingMovie = collectionRepository.findByTmdbId(movieBasicDTO.getId());
        if (existingMovie.isPresent()) {
            return existingMovie.get();
        }

        // getting DTOs
        MovieDetailsDTO movieDetailsDTO = omdbService.getMovieDetails(movieBasicDTO.getTitle(), movieBasicDTO.getRelease_date());
        List<MovieProviderDTO> movieProviderDTOs = tmdbService.searchProvidersInTMDB(movieBasicDTO.getId());

        // map to domain
        MovieDetails movieDetails = collectionMapper.movieDetailsDTOToMovieDetails(movieDetailsDTO);
        List<MovieProvider> movieProviders = collectionMapper.movieProviderDTOListToMovieProviderList(movieProviderDTOs);

        // creating Movie object
        Movie movie = new Movie();
        movie.setTmdbId(movieBasicDTO.getId());
        movie.setDetails(movieDetails);
        movie.setProviders(movieProviders);


        return movie;
    }

    public void updatePhysicalVersion(Long movieId, PhysicalVersionDTO physicalVersionDto) throws MovieNotFoundException {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));

        UserMovie userMovie = movieRepository.findByUserAndMovieId(currentUser, movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        PhysicalVersion physicalVersion = new PhysicalVersion();
        physicalVersion.setDescription(physicalVersionDto.getDescription());
        physicalVersion.setReleaseYear(physicalVersionDto.getReleaseYear());
        physicalVersion.setSteelbook(physicalVersionDto.getSteelbook());
        physicalVersion.setDetails(physicalVersionDto.getDetails());

        userMovie.setPhysicalVersion(physicalVersion);
        userActionService.publishUserActionEvent(currentUser, ActionType.ADD_PHYSICAL_VERSION);
        movieRepository.save(userMovie);
    }

    public List<PhysicalVersionDTO> getPhysicalVersions() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));
        List<UserMovie> userMovies = movieRepository.findByUser_Id(currentUser.getId());
        userActionService.publishUserActionEvent(currentUser, ActionType.GET_ALL_PHYSICAL_VERSIONS);
        return userMovies.stream()
                .filter(userMovie -> userMovie.getPhysicalVersion() != null) // tylko filmy z wersjami fizycznymi
                .map(userMovie -> new PhysicalVersionDTO(
                        userMovie.getPhysicalVersion().getDescription(),
                        userMovie.getPhysicalVersion().getReleaseYear(),
                        userMovie.getPhysicalVersion().getSteelbook(),
                        userMovie.getPhysicalVersion().getDetails(),
                        userMovie.getMovie().getDetails().getTitle() // nazwa filmu
                ))
                .collect(Collectors.toList());
    }
}
