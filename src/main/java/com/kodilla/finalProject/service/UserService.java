package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.*;
import com.kodilla.finalProject.errorHandling.*;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.mapper.UserMapper;
import com.kodilla.finalProject.repository.RoleRepository;
import com.kodilla.finalProject.repository.UserMovieRepository;
import com.kodilla.finalProject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserActionService userActionService;

    public User registerUser(User user) throws UsernameExistsException, EmailExistsException {
        //check exceptions
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameExistsException(user.getUsername());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailExistsException(user.getEmail());
        }
        List<Role> roles = user.getRoles().stream()
                .map(role -> roleRepository.findByName(role.getName().name())
                        .orElseThrow(() -> new RoleWithNameNotFoundException(role.getName().name())))
                .collect(Collectors.toList());
        //create user
        // Set roles for user
        user.setRoles(roles);
        //Set password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        //Set status
        user.setStatus(User.UserStatus.ACTIVE);
        userActionService.publishUserActionEvent(user, ActionType.REGISTER_USER);
        return userRepository.save(user);
    }

    public User createUser(User user) throws RoleWithNameNotFoundException { //Admin purposes
        List<Role> roles = user.getRoles().stream()
                .map(role -> roleRepository.findByName(role.getName().name())
                        .orElseThrow(() -> new RoleWithNameNotFoundException(role.getName().name())))
                .collect(Collectors.toList());

        user.setRoles(roles);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    public void addMovieToUserFavorites(Movie movie, User user) {
        if (user.getFavoriteMovies().contains(movie)) {
            throw new MovieInUsersListException(movie.getDetails().getTitle());
        }
        user.getFavoriteMovies().add(movie);
        userActionService.publishUserActionEvent(user, ActionType.ADD_TO_FAVORITES);
        userRepository.save(user);
    }

    public void updateUserStatus(Long userId, User.UserStatus userStatus) throws UserWithNameNotFoundException {
        // searching for user
        Optional<User> optionalUser = userRepository.findById(userId);

        // if user do not exist, throw exception
        if (optionalUser.isEmpty()) {
            throw new UserWithNameNotFoundException("User with Id " + userId + " not found.");
        }

        // set new status
        User user = optionalUser.get();
        user.setStatus(userStatus);
        userRepository.save(user);
    }

    public User updateUser(Long userId, UserDTO userDTO) throws UserWithNameNotFoundException, RoleWithNameNotFoundException {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new UserWithIdNotFoundException(userId);
        }

        User user = optionalUser.get();

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserWithNameNotFoundException(currentUsername));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(Role.RoleName.ADMIN));

        if (!isAdmin) {
            // when user is not Admin
            userDTO.setRoles(null);
            userDTO.setStatus(null);
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirst_name(userDTO.getFirst_name());
        user.setLast_name(userDTO.getLast_name());

        if (isAdmin && userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            List<Role> roles = userDTO.getRoles().stream()
                    .map(role -> roleRepository.findByName(role.getName().name())
                            .orElseThrow(() -> new RoleWithNameNotFoundException(role.getName().name())))
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }

        if (isAdmin && userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        userActionService.publishUserActionEvent(user, ActionType.UPDATE_PROFILE);

        return userRepository.save(user);
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
    public MovieCollectionStatsDTO getCollectionStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserWithIdNotFoundException(userId));

        List<Movie> favoriteMovies = user.getFavoriteMovies();

        if (favoriteMovies.isEmpty()) {
            return new MovieCollectionStatsDTO(0, "N/A", "N/A", "N/A");
        }

        //how many movies
        int totalMovies = favoriteMovies.size();

        //Most popular genre
        String mostCommonGenre = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getGenre())
                .filter(Objects::nonNull)
                .flatMap(genre -> Arrays.stream(genre.split(", ")))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Oldest and newest film
        Optional<String> oldestMovieYear = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getYear())
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());

        Optional<String> newestMovieYear = favoriteMovies.stream()
                .map(movie -> movie.getDetails().getYear())
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        userActionService.publishUserActionEvent(user, ActionType.GET_COLLECTION_STATS);

        return new MovieCollectionStatsDTO(
                totalMovies,
                mostCommonGenre,
                oldestMovieYear.orElse("N/A"),
                newestMovieYear.orElse("N/A")
        );
    }
}
