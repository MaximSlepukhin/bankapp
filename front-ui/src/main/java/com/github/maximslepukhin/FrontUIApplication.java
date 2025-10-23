package com.github.maximslepukhin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.github.maximslepukhin.client")

public class FrontUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontUIApplication.class, args);
    }
}