package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.*;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.repository.RoleRepository;
import com.kodilla.finalProject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserActionService userActionService;

    public User registerUser(UserDTO userDto, boolean assignRoles)
            throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {

        // if name exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameExistsException(userDto.getUsername());
        }

        // if email exists
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailExistsException(userDto.getEmail());
        }

        // list of roles
        List<Role> roles;

        if (assignRoles && userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            // roles from dto to entity
            roles = userDto.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseThrow(() -> new RoleWithNameNotFoundException(roleDto.getName().name())))
                    .collect(Collectors.toList());
        } else {
            // if there is no role, set "USER"
            Role defaultRole = roleRepository.findByName(Role.RoleName.USER)
                    .orElseThrow(() -> new RoleWithNameNotFoundException(Role.RoleName.USER.name()));
            roles = List.of(defaultRole);
        }

        // map userDTO to user
        User user = userMapper.mapUserDtoToUser(userDto);
        user.setRoles(roles);

        // encode password
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(encodedPassword);

        // event
        userActionService.publishUserActionEvent(user, ActionType.REGISTER_USER);

        // save to database
        return userRepository.save(user);
    }
    public User updateUser(Long userId, UserDTO userDto, boolean assignRoles)
            throws UserWithIdNotFoundException, UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {

        // find user
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserWithIdNotFoundException(userId));

        // check for duplicates if username is changed
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
                userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameExistsException(userDto.getUsername());
        }

        // check for duplicates if email is changed
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailExistsException(userDto.getEmail());
        }

        List<Role> roles = existingUser.getRoles();  // Default to current roles
        if (assignRoles && userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            roles = userDto.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseThrow(() -> new RoleWithNameNotFoundException(roleDto.getName().name())))
                    .collect(Collectors.toList());
        }

        // Update user details
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirst_name(userDto.getFirst_name());
        existingUser.setLast_name(userDto.getLast_name());
        existingUser.setRoles(roles); // Set roles only if appropriate

        // Event
        userActionService.publishUserActionEvent(existingUser, ActionType.UPDATE_PROFILE);

        // Save and return updated user
        return userRepository.save(existingUser);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.mapUserListToUserDTOList(users);
    }

    public UserDTO getUserById(Long userId) throws UserWithNameNotFoundException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserWithIdNotFoundException(userId);
        }
        User user = optionalUser.get();
        return userMapper.mapUserToUserDto(user);
    }

    public boolean deleteUserById(final Long userId) throws UserWithIdNotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserWithIdNotFoundException(userId);
        }
        User user = userOptional.get();

        userRepository.deleteById(userId);

        userActionService.publishUserActionEvent(user, ActionType.DELETE_PROFILE);

    return true;
    }
}
