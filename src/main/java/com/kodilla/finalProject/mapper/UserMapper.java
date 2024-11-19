package com.kodilla.finalProject.mapper;

import com.kodilla.finalProject.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserMapper {
    public User mapUserDtoToUser(UserDTO userDto) {
        return User.builder()
                .first_name(userDto.getFirst_name())
                .last_name(userDto.getLast_name())
                .email(userDto.getEmail())
                .username(userDto.getUsername())
                .status(userDto.getStatus())
                .roles(userDto.getRoles().stream().map(this::roleDTOToRole)
                .collect(Collectors.toList()))
                .build();
    }
    public UserDTO mapUserToUserDto(User user) {
        return new UserDTO(user.getFirst_name(),
                user.getLast_name(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(this::roleToRoleDTO)
                        .collect(Collectors.toList()));
    }
    public Role roleDTOToRole(RoleDTO roleDTO) {
        return new Role(null, roleDTO.getName());
    }
    public RoleDTO roleToRoleDTO(Role role) {
        return new RoleDTO(role.getName());
    }
    public List<RoleDTO> rolesToRoleDTOs(List<Role> roles) {
        List<RoleDTO> roleDTOs = new ArrayList<>();
        for (Role role : roles) {
            roleDTOs.add(roleToRoleDTO(role));
        }
        return roleDTOs;
    }
    public List<UserDTO> mapUserListToUserDTOList(List<User> userList) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : userList) {
            userDTOList.add(mapUserToUserDto(user));
        }
        return userDTOList;
    }

}