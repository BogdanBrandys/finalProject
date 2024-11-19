package com.kodilla.finalProject.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieCollectionStatsDTO {
    private int totalMovies;
    private String mostCommonGenre;
    private String oldestMovieYear;
    private String newestMovieYear;

}
