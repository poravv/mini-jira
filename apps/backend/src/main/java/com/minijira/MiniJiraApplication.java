package com.minijira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la aplicación: arranca Spring Boot, que descubre los módulos bajo com.minijira. */
@SpringBootApplication
public class MiniJiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniJiraApplication.class, args);
    }
}
