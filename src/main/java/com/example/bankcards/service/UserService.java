package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Метод для регистрации нового пользователя
    public User registerUser(String username, String password, String email) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already exists");
        }

        User user = new User(username, password, email);
        return userRepository.save(user);
    }

    // Метод для поиска пользователя по имени
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Метод для поиска пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
