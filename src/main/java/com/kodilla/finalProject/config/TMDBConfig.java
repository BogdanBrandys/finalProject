package com.kodilla.finalProject.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TMDBConfig {
    @Value("${tmdb.api.endpoint.prod}")
    private String tmdbApiEndpoint;
    @Value("${tmdb.app.key}")
    private String tmdbAppKey;
}
