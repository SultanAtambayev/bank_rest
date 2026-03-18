package com.example.bankcards.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    private final String jwtSecret = "my-super-secret-key"; // из application.yml
    private final long jwtExpirationMs = 3600000; // 1 час

    // Генерация токена
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Получение username из токена
    public String getUsernameFromJwt(String token) {
        return parseClaims(token).getSubject();
    }

    // Получение роли из токена
    public String getRoleFromJwt(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Проверка валидности токена
    public boolean validateJwtToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
