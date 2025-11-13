// src/main/java/com/example/config/DataSeeder.java
package com.example.config;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(RoleRepository roles, UserRepository users, PasswordEncoder pe) {
        return args -> {
            Role admin = roles.findByName("ADMIN").orElseGet(() -> roles.save(new Role("ADMIN")));
            Role user  = roles.findByName("USER").orElseGet(() -> roles.save(new Role("USER")));

            users.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(pe.encode("admin")); // mot de passe: admin
                u.setEnabled(true);
                u.setRoles(Set.of(admin));
                return users.save(u);
            });
        };
    }
}
