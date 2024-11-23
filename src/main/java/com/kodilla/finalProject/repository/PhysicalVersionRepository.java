package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.PhysicalVersion;
import org.springframework.data.repository.CrudRepository;

public interface PhysicalVersionRepository extends CrudRepository<PhysicalVersion, Long> {
    @Override
    PhysicalVersion save(PhysicalVersion movie);
}
