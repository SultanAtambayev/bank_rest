package com.example.bankcards.util;

import java.util.Base64;

public class EncodeSecret {
    public static void main(String[] args) {
        // Введи здесь свой текущий секрет
        String secret = "my-super-secret-key";

        // Генерация Base64
        String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());

        // Вывод результата в консоль
        System.out.println("Base64-секрет: " + base64Secret);
    }
}
