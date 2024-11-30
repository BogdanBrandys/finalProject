package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.Movie;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserMovie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieRepository extends CrudRepository<UserMovie, Long> {
    Optional<UserMovie> findByUserAndMovieId(User user, Long movieId);
    List<UserMovie> findByUser_Id(Long userId);
    Optional<UserMovie> findByUserAndMovie(User user, Movie movie);
}
