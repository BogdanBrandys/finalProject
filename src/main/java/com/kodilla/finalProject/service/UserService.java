package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.*;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.repository.RoleRepository;
import com.kodilla.finalProject.repository.UserMovieRepository;
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
    private final UserMovieRepository userMovieRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserActionService userActionService;

    public User registerUser(UserDTO userDto, boolean assignRoles)
            throws UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {

        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameExistsException(userDto.getUsername());
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailExistsException(userDto.getEmail());
        }

        List<Role> roles = new ArrayList<>();
        if (assignRoles && userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            roles = userDto.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseGet(() -> {
                                System.out.println("Nie znaleziono roli: " + roleDto.getName());
                                return null;
                            }))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RoleWithNameNotFoundException("USER"));
            roles.add(defaultRole);
        }

        User user = userMapper.mapUserDtoToUser(userDto);
        user.setRoles(roles);

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(encodedPassword);

        userActionService.publishUserActionEvent(user, ActionType.REGISTER_USER);

        return userRepository.save(user);
    }
    public User updateUser(Long userId, UserDTO userDto, boolean assignRoles)
            throws UserWithIdNotFoundException, UsernameExistsException, EmailExistsException, RoleWithNameNotFoundException {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserWithIdNotFoundException(userId));

        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
                userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameExistsException(userDto.getUsername());
        }

        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailExistsException(userDto.getEmail());
        }

        List<Role> roles = new ArrayList<>(existingUser.getRoles()); // Domyślnie obecne role
        if (assignRoles && userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            roles = userDto.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseGet(() -> {
                                System.out.println("Nie znaleziono roli: " + roleDto.getName());
                                return null;
                            }))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RoleWithNameNotFoundException("USER"));
            roles.add(defaultRole);
        }

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirst_name(userDto.getFirst_name());
        existingUser.setLast_name(userDto.getLast_name());
        existingUser.setRoles(roles); // Set roles only if appropriate

        userActionService.publishUserActionEvent(existingUser, ActionType.UPDATE_PROFILE);

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

        for (UserMovie userMovie : user.getUserMovies()) {
            userMovie.setUser(null);  // Odłączamy użytkownika od filmu
            userMovieRepository.save(userMovie);  // Zapisujemy zmiany w bazie
        }
        userRepository.deleteById(userId);

        userActionService.publishUserActionEvent(user, ActionType.DELETE_PROFILE);

    return true;
    }
}
