package com.kodilla.finalProject.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OMDBConfig {
    @Value("${omdb.api.endpoint.prod}")
    private String omdbApiEndpoint;
    @Value("${omdb.app.key}")
    private String omdbAppKey;
}
