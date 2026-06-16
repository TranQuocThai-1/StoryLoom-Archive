package com.storyloom.archive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF security specifically for the AI API endpoints so the JS fetch works
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/ai/**"))
            
            .authorizeHttpRequests(auth -> auth
                // 2. Whitelist the /api/ai/** paths so anyone can chat with the Librarian
                .requestMatchers("/", "/search/**", "/catalog/**", "/book/**", "/read/**", "/author/**", "/category/**", "/style.css", "/app.js", "/api/ai/**").permitAll()
                .requestMatchers("/login", "/register", "/about", "/contact", "/faq", "/terms", "/privacy", "/permissions", "/error").permitAll()
                
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .maximumSessions(1) 
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired=true") 
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email") 
                .defaultSuccessUrl("/", true) 
                .failureUrl("/login?error=true") 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}