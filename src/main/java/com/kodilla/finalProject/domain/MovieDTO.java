package com.kodilla.finalProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Long movie_id;
    private Long tmdbId;
    private MovieDetailsDTO details;
    private GroupedProvidersDTO providers;
}
