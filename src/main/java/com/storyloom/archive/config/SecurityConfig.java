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

    // 1. Password Hashing: Never store plain text passwords!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // We need the @Bean annotation here so Spring uses this to track active sessions
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // 2. The Traffic Cop: Decides which URLs are public and which require login
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow everyone to access the homepage, search, catalogs, books, info pages, and styles
                .requestMatchers("/", "/search/**", "/catalog/**", "/book/**", "/author/**", "/category/**", "/style.css").permitAll()
                .requestMatchers("/login", "/register", "/about", "/contact", "/faq", "/terms", "/privacy", "/permissions", "/error").permitAll()
                
                // Only allow Admins to access the admin dashboard
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Any other route requires the user to be logged in
                .anyRequest().authenticated()
            )
            // --- SESSION MANAGEMENT ---
            .sessionManagement(session -> session
                .maximumSessions(1) 
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired=true") 
            )
            // --------------------------
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email") // Important: Our login form uses "email" instead of "username"
                .defaultSuccessUrl("/", true) // Send them to the homepage after logging in
                .failureUrl("/login?error=true") // Send them back to login if they fail
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