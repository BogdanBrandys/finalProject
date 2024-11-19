package com.kodilla.finalProject.TMDB.client;

import com.kodilla.finalProject.config.TMDBConfig;
import com.kodilla.finalProject.domain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TMDBClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMDBClient.class);
    private final RestTemplate restTemplate;
    private final TMDBConfig tmdbConfig;

    public List<MovieBasicDTO> searchMovie(String title) {
        URI url = UriComponentsBuilder.fromHttpUrl(tmdbConfig.getTmdbApiEndpoint() + "/search/movie")
                .queryParam("api_key", tmdbConfig.getTmdbAppKey())
                .queryParam("query", title)
                .build()
                .encode()
                .toUri();
        LOGGER.info("Request URL: " + url.toString());

        try {
            TMDBResponseDTO response = restTemplate.getForObject(url, TMDBResponseDTO.class);

            if (response != null && response.getResults() != null) {
                LOGGER.info("Number of movies returned: " + response.getResults().size());
            } else {
                LOGGER.warn("No movies returned from TMDB.");
            }

            return Optional.ofNullable(response)
                    .map(TMDBResponseDTO::getResults)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(movie -> Objects.nonNull(movie.getId()) && Objects.nonNull(movie.getTitle()))
                    .collect(Collectors.toList());

        } catch (RestClientException e) {
            LOGGER.error("Error fetching movies from TMDB: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    public List<MovieProviderDTO> searchProviders(Long tmdbId) {
        URI url = UriComponentsBuilder.fromHttpUrl(tmdbConfig.getTmdbApiEndpoint() + "/movie/" + tmdbId + "/watch/providers")
                .queryParam("api_key", tmdbConfig.getTmdbAppKey())
                .build()
                .encode()
                .toUri();
        LOGGER.info("Request URL: " + url.toString());

        List<MovieProviderDTO> providers = new ArrayList<>();
        try {
            MovieProviderResponseDTO response = restTemplate.getForObject(url, MovieProviderResponseDTO.class);

            if (response != null && response.getResults() != null) {
                LOGGER.info("Number of providers returned: " + response.getResults().size());
            } else {
                LOGGER.warn("No providers returned from TMDB.");
            }

            Optional.ofNullable(response)
                    .map(MovieProviderResponseDTO::getResults)
                    .map(results -> results.get("PL"))
                    .ifPresent(plData -> {
                        // Sprawdź, czy są dane dla typu RENT, BUY, FLATRATE
                        if (plData.getRent() != null) {
                            providers.addAll(plData.getRent().stream()
                                    .map(provider -> new MovieProviderDTO(provider.getProvider_name(), MovieProvider.AccessType.RENTAL))
                                    .collect(Collectors.toList()));
                        }

                        if (plData.getBuy() != null) {
                            providers.addAll(plData.getBuy().stream()
                                    .map(provider -> new MovieProviderDTO(provider.getProvider_name(), MovieProvider.AccessType.PURCHASE))
                                    .collect(Collectors.toList()));
                        }

                        if (plData.getFlatrate() != null) {
                            providers.addAll(plData.getFlatrate().stream()
                                    .map(provider -> new MovieProviderDTO(provider.getProvider_name(), MovieProvider.AccessType.SUBSCRIPTION))
                                    .collect(Collectors.toList()));
                        }
                    });

            return providers;

        } catch (RestClientException e) {
            LOGGER.error("Error fetching providers from TMDB: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
