package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на аутентификацию")
public class LoginRequest {

    @Schema(description = "Имя пользователя", example = "john_doe", required = true)
    @NotBlank(message = "Username обязателен")
    private String username;

    @Schema(description = "Пароль", example = "password123", required = true)
    @NotBlank(message = "Password обязателен")
    private String password;

    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
