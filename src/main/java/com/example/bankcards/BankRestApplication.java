package com.example.bankcards;

import com.example.bankcards.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BankRestApplication {


    public static void main(String[] args) {

        SpringApplication.run(BankRestApplication.class, args);
    }

}

