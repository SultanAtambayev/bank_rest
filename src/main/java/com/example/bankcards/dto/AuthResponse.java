package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации с JWT токеном")
public class AuthResponse {

    @Schema(description = "JWT токен доступа")
    private String token;

    @Schema(description = "Тип токена")
    private String type = "Bearer";

    @Schema(description = "Имя пользователя")
    private String username;

    @Schema(description = "Роль пользователя")
    private String role;

    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Геттеры
    public String getToken() { return token; }
    public String getType() { return type; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
