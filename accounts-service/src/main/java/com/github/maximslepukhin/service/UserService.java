package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByLogin(String login);

    List<UserDto> getAllUsers();

    UserDto findByLogin(String login);

    UserDto findByKeycloakId(String keycloakId);

    UserDto updateUser(String login, UserDto userDto);
}
