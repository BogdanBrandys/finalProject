package com.kodilla.finalProject.OMDB.client;

import com.kodilla.finalProject.TMDB.client.TMDBClient;
import com.kodilla.finalProject.config.OMDBConfig;
import com.kodilla.finalProject.domain.MovieDetailsDTO;
import com.kodilla.finalProject.domain.RatingDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OMDBClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMDBClient.class);
    private final RestTemplate restTemplate;
    private final OMDBConfig omdbConfig;

    public MovieDetailsDTO fetchMovieDetailsByTitleAndYear(String title, String year) {
        URI url = UriComponentsBuilder.fromHttpUrl(omdbConfig.getOmdbApiEndpoint())
                .queryParam("apikey", omdbConfig.getOmdbAppKey())
                .queryParam("t", title)
                .queryParam("y", year)
                .build()
                .encode()
                .toUri();
        LOGGER.info("Request URL: " + url.toString());
        try {
            MovieDetailsDTO response = restTemplate.getForObject(url, MovieDetailsDTO.class);
            System.out.println("OMDB response: " + response);
        if (response != null && response.getTitle() != null) {
            LOGGER.info("Successfully fetched movie details from OMDB.");

            if (response.getRatings() != null) {
                List<RatingDTO> ratingDTOs = response.getRatings().stream()
                        .map(rating -> new RatingDTO(rating.getSource(), rating.getValue()))
                        .collect(Collectors.toList());
                response.setRatings(ratingDTOs);
            } else {
                response.setRatings(Collections.emptyList());
            }

            return response;

        } else {
            LOGGER.warn("No matching movie found in OMDB for title: " + title + " and year: " + year);
            return new MovieDetailsDTO(title, null, year, null, null, Collections.emptyList());
        }

    } catch (RestClientException e) {
        LOGGER.error("Error fetching movie details from OMDB: " + e.getMessage(), e);
        return new MovieDetailsDTO(title, null, year, null, null, Collections.emptyList());
    }
}
}
