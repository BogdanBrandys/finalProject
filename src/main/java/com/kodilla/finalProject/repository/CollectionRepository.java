package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.Movie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends CrudRepository<Movie, Long> {
    @Override
    List<Movie> findAll();
    @Override
    Movie save(Movie movie);
    @Override
    Optional<Movie> findById(Long id);
    @Override
    void deleteById(Long id);
    Optional<Movie> findByTmdbId(Long tmdbId);
}
