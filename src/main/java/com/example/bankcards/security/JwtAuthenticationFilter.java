package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Логируем заголовок для отладки
        System.out.println("=== AUTH HEADER: " + authHeader + " ===");

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("=== TOKEN EXTRACTED: " + token.substring(0, Math.min(50, token.length())) + "... ===");

            try {
                username = jwtUtils.getUsernameFromToken(token);
                System.out.println("=== USERNAME FROM TOKEN: " + username + " ===");
            } catch (Exception e) {
                System.out.println("=== JWT ERROR: " + e.getMessage() + " ===");
                logger.error("JWT невалиден", e);
            }
        } else {
            System.out.println("=== NO BEARER TOKEN FOUND ===");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = jwtUtils.getRoleFromToken(token);
            System.out.println("=== ROLE FROM TOKEN: " + role + " ===");

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("=== AUTHENTICATION SET FOR USER: " + username + " ===");
        }

        filterChain.doFilter(request, response);
    }
}