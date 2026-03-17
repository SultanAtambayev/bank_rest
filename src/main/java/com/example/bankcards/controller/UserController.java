package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email) {

        // Создаем объект User
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // Регистрируем через сервис
        User savedUser = userService.registerUser(user);

        return ResponseEntity.ok(savedUser);
    }
}
