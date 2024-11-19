package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.errorHandling.RoleAlreadyExistsException;
import com.kodilla.finalProject.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role addRole(Role.RoleName roleName) {

        if (roleRepository.findByName(roleName.name()).isPresent()) {
            throw new RoleAlreadyExistsException(roleName.name());
        }

        Role role = new Role(null, roleName);
        return roleRepository.save(role);
    }

    public boolean deleteRole(Long roleId) {

        Optional<Role> role = roleRepository.findById(roleId);

        if (role.isPresent()) {
            roleRepository.delete(role.get());
            return true;
        }
        return false;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}

