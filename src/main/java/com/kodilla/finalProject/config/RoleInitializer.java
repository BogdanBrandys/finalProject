package com.kodilla.finalProject.config;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {

            Role userRole = new Role();
            userRole.setName(Role.RoleName.USER);
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
        }
    }
}