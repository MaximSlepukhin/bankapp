package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.UserDto;

public interface UserService {
    UserDto getUserByLogin(String login);
    UserDto createUser(UserDto userDto);
}