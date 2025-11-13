package com.example.config;

import com.example.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Configuration de la sécurité avec Spring Security
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Bean pour encoder les mots de passe avec BCrypt
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // Bean pour charger les détails de l'utilisateur depuis la base de données
    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRoles().stream()
                                .map(r -> "ROLE_" + r.getName()).toArray(String[]::new))
                        .disabled(!u.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Configuration des règles de sécurité HTTP
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login","/css/**","/js/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/clients/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/clients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                )
                .logout(l -> l
                        .logoutUrl("/logout")                 // POST /logout
                        .logoutSuccessUrl("/login?logout")    // où rediriger après logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }
}
