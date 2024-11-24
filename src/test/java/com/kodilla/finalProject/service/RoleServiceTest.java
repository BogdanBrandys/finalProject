package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.errorHandling.RoleAlreadyExistsException;
import com.kodilla.finalProject.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void testAddRoleShouldAddRole() {
        // Given
        String roleName = "ADMIN";
        Role role = new Role(null, roleName);
        // When
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
        when(roleRepository.save(role)).thenReturn(role);
        Role result = roleService.addRole(roleName);

        // Then
        assertNotNull(result);
        assertEquals(roleName, result.getName());
        verify(roleRepository).save(role);
    }

    @Test
    void testAddRoleShouldThrowExceptionWhenRoleExists() {
        // Given
        String roleName = "ADMIN";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(new Role(1L, roleName)));

        // When & Then
        RoleAlreadyExistsException exception = assertThrows(RoleAlreadyExistsException.class, () -> roleService.addRole(roleName));
        assertEquals("Role " + roleName + " already exists", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testDeleteRoleShouldReturnTrueWhenRoleExists() {
        // Given
        Long roleId = 1L;
        Role role = new Role(roleId, "ADMIN");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // When
        boolean result = roleService.deleteRole(roleId);

        // Then
        assertTrue(result);
        verify(roleRepository).delete(role);
    }

    @Test
    void testDeleteRoleShouldReturnFalseWhenRoleNotFound() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When
        boolean result = roleService.deleteRole(roleId);

        // Then
        assertFalse(result);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void testGetAllRoles() {
        // Given
        List<Role> roles = List.of(
                new Role(1L, "ADMIN"),
                new Role(2L, "USER")
        );
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<Role> result = roleService.getAllRoles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        verify(roleRepository).findAll();
    }
}