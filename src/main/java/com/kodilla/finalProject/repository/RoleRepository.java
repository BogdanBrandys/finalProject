package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.domain.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName name);
    @Override
    List<Role> findAll();
}
