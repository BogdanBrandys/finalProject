package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.PhysicalVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhysicalVersionRepository extends CrudRepository<PhysicalVersion, Long> {
    @Override
    List<PhysicalVersion> findAll();

}
