package com.kodilla.finalProject.service;

import com.kodilla.finalProject.TMDB.client.TMDBClient;
import com.kodilla.finalProject.domain.MovieBasicDTO;
import com.kodilla.finalProject.domain.MovieProviderDTO;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.errorHandling.UserWithNameNotFoundException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TMDBService {

    private final TMDBClient tmdbClient;
    private final UserActionService userActionService;
    private final UserRepository userRepository;

    public List<MovieBasicDTO> searchMoviesInTMDB(String title) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithNameNotFoundException(username));
        userActionService.publishUserActionEvent(user, ActionType.SEARCH_MOVIE);
        return tmdbClient.searchMovie(title);

    }

    public List<MovieProviderDTO> searchProvidersInTMDB(Long tmdbId) {
        return tmdbClient.searchProviders(tmdbId);
    }

}
