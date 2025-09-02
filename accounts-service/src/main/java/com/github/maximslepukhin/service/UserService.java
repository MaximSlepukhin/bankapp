package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    public User registerUser(String login, String password, String name, LocalDate birthdate) {
        if (Period.between(birthdate, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Пользователю должно быть не менее 18 лет");
        }
        User user = new User();
        user.setLogin(login);
        user.setPassword(password); // позже будем хешировать!
        user.setName(name);
        user.setBirthdate(birthdate);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}