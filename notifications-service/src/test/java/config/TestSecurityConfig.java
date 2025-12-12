package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()  // Разрешаем все запросы без аутентификации
                .and()
                .csrf(csrf -> csrf.disable());  // Отключаем CSRF для тестов

        return http.build();  // Возвращаем конфигурированный фильтр безопасности
    }
}