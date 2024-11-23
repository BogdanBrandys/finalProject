package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.MovieDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MovieDetailsRepository extends CrudRepository<MovieDetails, Long> {
    Optional<MovieDetails> findByTitleAndYear(String title, String Year);
    @Override
    MovieDetails save(MovieDetails movieDetails);
}
