package com.kodilla.finalProject.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity(name = "movie_details")
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long details_id;
    private String title;
    private String genre;
    private String year;
    private String director;
    private String plot;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Rating> ratings;

    @OneToOne(mappedBy = "details")
    private Movie movie;
}
