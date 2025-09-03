package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.UserDto;

public interface UserService {
    /**
     * Получить данные пользователя по логину в виде DTO.
     *
     * @param login логин пользователя
     * @return UserDto с данными пользователя и его счетов
     */
    UserDto getUserByLogin(String login);
}