package com.kodilla.finalProject.controller;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.domain.RoleDTO;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final UserMapper userMapper;

    @Operation(description = "Add new role to the database",
            summary = "Add role"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RoleDTO> addRole(@RequestBody RoleDTO roleDTO) {
        Role role = roleService.addRole(roleDTO.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.roleToRoleDTO(role));
    }

    @Operation(description = "Delete role by its Id",
            summary = "Delete role"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        boolean isDeleted = roleService.deleteRole(roleId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(description = "Get All roles from database",
            summary = "Get all roles"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(userMapper.rolesToRoleDTOs(roles));
    }
}
