package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.Movie;
import com.kodilla.finalProject.domain.PhysicalVersion;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserMovie;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PhysicalVersionRepository extends CrudRepository<PhysicalVersion, Long> {
    @Override
    PhysicalVersion save(PhysicalVersion movie);
}
