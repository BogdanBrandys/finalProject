package com.kodilla.finalProject.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "movies")
public class Movie {
        @Id
        @GeneratedValue
        private Long movie_id;
        private Long tmdbId;
        //omdb data
        @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "details_id")
        private MovieDetails details;
        //tmdb data
        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<MovieProvider> providers;

        @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "physical_version_id")
        private PhysicalVersion physicalVersion;
}