package ru.ilyshka_fox.service.cashback.vk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class VkClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(VkClientApplication.class, args);
    }
}
