package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.MovieProvider;
import org.springframework.data.repository.CrudRepository;

public interface MovieProviderRepository extends CrudRepository<MovieProvider, Long> {
}
