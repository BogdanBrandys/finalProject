package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.Role;
import com.kodilla.finalProject.domain.RoleDTO;
import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.domain.UserDTO;
import com.kodilla.finalProject.errorHandling.EmailExistsException;
import com.kodilla.finalProject.errorHandling.RoleWithNameNotFoundException;
import com.kodilla.finalProject.errorHandling.UserWithIdNotFoundException;
import com.kodilla.finalProject.errorHandling.UsernameExistsException;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.repository.RoleRepository;
import com.kodilla.finalProject.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserActionService userActionService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserDTO userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDTO("John",
                "Smith","john_smith@example.com",
                "JohnSmith","john1234", null);

        user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirst_name(userDto.getFirst_name());
        user.setLast_name(userDto.getLast_name());
        user.setPassword(userDto.getPassword());
        user.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    void registerTest_shouldRegisterUserWithDefaultRole() throws Exception {
        // Given
        Role defaultRole = new Role(1L, Role.RoleName.USER);
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(defaultRole));
        when(userMapper.mapUserDtoToUser(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        // When
        User registeredUser = userService.registerUser(userDto, false);

        // Then
        assertNotNull(registeredUser);
        assertEquals("JohnSmith", registeredUser.getUsername());
        assertEquals(1, registeredUser.getRoles().size());
        assertEquals(Role.RoleName.USER, registeredUser.getRoles().get(0).getName());

        verify(userActionService).publishUserActionEvent(user, ActionType.REGISTER_USER);
        verify(userRepository).save(user);
    }

    @Test
    void registerTest_shouldRegisterUserWithCustomRoles() throws Exception {
        // Given
        Role adminRole = new Role(2L, Role.RoleName.ADMIN);
        userDto.setRoles(List.of(new RoleDTO(Role.RoleName.ADMIN)));

        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userMapper.mapUserDtoToUser(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        // When
        User registeredUser = userService.registerUser(userDto, true);

        // Then
        assertNotNull(registeredUser);
        assertEquals(1, registeredUser.getRoles().size());
        assertEquals(Role.RoleName.ADMIN, registeredUser.getRoles().get(0).getName());

        verify(userActionService).publishUserActionEvent(user, ActionType.REGISTER_USER);
        verify(userRepository).save(user);
    }

    @Test
    void registerTest_shouldThrowExceptionWhenUsernameExists() {
        // Given
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(new User()));

        // When & Then
        UsernameExistsException exception = Assertions.assertThrows(UsernameExistsException.class, () ->
                userService.registerUser(userDto, false)
        );

        assertEquals("JohnSmith", exception.getUsername());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void registerTest_shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(new User()));

        // When & Then
        EmailExistsException exception = Assertions.assertThrows(EmailExistsException.class, () ->
                userService.registerUser(userDto, false)
        );

        assertEquals("john_smith@example.com", exception.getEmail());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void registerTest_shouldThrowExceptionWhenRoleNotFound() {
        // Given
        userDto.setRoles(List.of(new RoleDTO(Role.RoleName.ADMIN)));
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.empty());

        // When & Then
        RoleWithNameNotFoundException exception = Assertions.assertThrows(RoleWithNameNotFoundException.class, () ->
                userService.registerUser(userDto, true)
        );

        assertEquals(Role.RoleName.ADMIN.name(), exception.getRoleName());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateTest_shouldUpdateUserWithNewRoles() throws Exception {
        // Given
        Long userId = 1L;
        Role newRole = new Role(2L, Role.RoleName.ADMIN);
        User existingUser = new User();
        existingUser.setUsername("JohnSmith");
        existingUser.setEmail("john_smith@example.com");
        existingUser.setRoles(List.of(new Role(1L, Role.RoleName.USER)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.of(newRole));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        userDto.setRoles(List.of(new RoleDTO(Role.RoleName.ADMIN)));

        // When
        User updatedUser = userService.updateUser(userId, userDto, true);

        // Then
        assertNotNull(updatedUser);
        assertEquals("JohnSmith", updatedUser.getUsername());
        assertEquals(1, updatedUser.getRoles().size());
        assertEquals(Role.RoleName.ADMIN, updatedUser.getRoles().get(0).getName());

        verify(userActionService).publishUserActionEvent(existingUser, ActionType.UPDATE_PROFILE);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateTest_shouldUpdateUserWithDefaultRoleIfNoRoleProvided() throws Exception {
        // Given
        Long userId = 1L;
        Role defaultRole = new Role(1L, Role.RoleName.USER);
        User existingUser = new User();
        existingUser.setUsername("JohnSmith");
        existingUser.setEmail("john_smith@example.com");
        existingUser.setRoles(List.of(new Role(1L, Role.RoleName.USER)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(defaultRole));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // When
        User updatedUser = userService.updateUser(userId, userDto, false);

        // Then
        assertNotNull(updatedUser);
        assertEquals(1, updatedUser.getRoles().size());
        assertEquals(Role.RoleName.USER, updatedUser.getRoles().get(0).getName());

        verify(userActionService).publishUserActionEvent(existingUser, ActionType.UPDATE_PROFILE);
        verify(userRepository).save(existingUser);
    }
    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(user);
        List<UserDTO> userDTOs = Arrays.asList(userDto);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.mapUserListToUserDTOList(users)).thenReturn(userDTOs);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("JohnSmith", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_UserExists() throws UserWithIdNotFoundException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapUserToUserDto(user)).thenReturn(userDto);

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("JohnSmith", result.getUsername());
        assertEquals("john_smith@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        Assertions.assertThrows(UserWithIdNotFoundException.class, () -> {
            userService.getUserById(1L);
        });
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteUserById_UserExists() throws UserWithIdNotFoundException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        boolean result = userService.deleteUserById(1L);

        // Then
        Assertions.assertTrue(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
        verify(userActionService, times(1)).publishUserActionEvent(user, ActionType.DELETE_PROFILE);
    }

    @Test
    void testDeleteUserById_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        Assertions.assertThrows(UserWithIdNotFoundException.class, () -> {
            userService.deleteUserById(1L);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(0)).deleteById(anyLong());
    }
}