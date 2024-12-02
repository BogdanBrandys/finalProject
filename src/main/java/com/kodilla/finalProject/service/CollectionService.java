package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.MovieInUsersListException;
import com.kodilla.finalProject.errorHandling.MovieNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithIdNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.CollectionMapper;
import com.kodilla.finalProject.repository.UserMovieRepository;
import com.kodilla.finalProject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CollectionService {

    private final UserRepository userRepository;
    private final UserMovieRepository userMovieRepository;
    private final UserActionService userActionService;
    private final CollectionMapper collectionMapper;
    private final DbService dbService;

    public List<MovieDTO> getAllMoviesFromFavourites() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));
        userActionService.publishUserActionEvent(user, ActionType.VIEW_FAVORITES);

        List<PhysicalVersionDTO> physicalVersions = dbService.getPhysicalVersions();

        return user.getFavoriteMovies().stream()
                .map(movie -> {
                    PhysicalVersionDTO physicalVersionDTO = physicalVersions.stream()
                            .filter(physicalVersion -> physicalVersion.getMovieTitle().equals(movie.getDetails().getTitle()))
                            .findFirst()
                            .orElse(null);

                    MovieDTO movieDTO = collectionMapper.mapToMovieDTO(movie);

                    movieDTO.setPhysicalVersion(physicalVersionDTO);

                    return movieDTO;
                })
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieFromFavourites(final Long id) throws MovieNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));

        List<Movie> movies = user.getFavoriteMovies();
        Movie movie = movies.stream()
                .filter(m -> m.getId() != null && m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException(id));

        List<PhysicalVersionDTO> physicalVersions = dbService.getPhysicalVersions();
        PhysicalVersionDTO physicalVersionDTO = physicalVersions.stream()
                .filter(physicalVersion -> physicalVersion.getMovieTitle().equals(movie.getDetails().getTitle()))
                .findFirst()
                .orElse(null);

        MovieDTO movieDTO = collectionMapper.mapToMovieDTO(movie);

        movieDTO.setPhysicalVersion(physicalVersionDTO);
        userActionService.publishUserActionEvent(user, ActionType.VIEW_FAVORITES);
        return movieDTO;
    }

    public void addMovieToUserFavorites(Movie movie, User user) {
        if (user.getFavoriteMovies().contains(movie)) {
            throw new MovieInUsersListException(movie.getDetails().getTitle());
        }
        user.getFavoriteMovies().add(movie);
        userActionService.publishUserActionEvent(user, ActionType.ADD_TO_FAVORITES);
        userRepository.save(user);
    }
    public boolean deleteMovieFromFavourites(final Long id) throws MovieNotFoundException, UserWithNameNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));

        List<Movie> favoriteMovies = user.getFavoriteMovies();

        boolean movieRemoved = favoriteMovies.removeIf(movie -> movie.getId().equals(id));

        if (!movieRemoved) {
            throw new MovieNotFoundException(id);
        }

        userRepository.save(user);
        userActionService.publishUserActionEvent(user, ActionType.REMOVE_FROM_FAVORITES);
        return true;
    }

    public MovieCollectionStatsDTO getCollectionStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserWithIdNotFoundException(userId));

        List<Movie> favoriteMovies = user.getFavoriteMovies();

        if (favoriteMovies.isEmpty()) {
            return new MovieCollectionStatsDTO(0, "N/A", "N/A", "N/A");
        }

        //how many movies
        int totalMovies = favoriteMovies.size();

        //Most popular genre
        String mostCommonGenre = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getGenre())
                .filter(Objects::nonNull)
                .flatMap(genre -> Arrays.stream(genre.split(", ")))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Oldest and newest film
        Optional<String> oldestMovieYear = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getYear())
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());

        Optional<String> newestMovieYear = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getYear())
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        userActionService.publishUserActionEvent(user, ActionType.GET_COLLECTION_STATS);

        return new MovieCollectionStatsDTO(
                totalMovies,
                mostCommonGenre,
                oldestMovieYear.orElse("N/A"),
                newestMovieYear.orElse("N/A")
        );
    }
}
