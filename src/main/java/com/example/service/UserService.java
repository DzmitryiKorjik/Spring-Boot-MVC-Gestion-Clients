package com.example.service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.model.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

// Service pour la gestion des utilisateurs
@Service
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder pe;

    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder pe) {
        this.users = users;
        this.roles = roles;
        this.pe = pe;
    }

    /** Crée un utilisateur avec le rôle USER par défaut */
    public void createUser(RegisterForm form) {
        // vérif username unique
        users.findByUsername(form.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("username_exists");
        });

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("password_mismatch");
        }

        Role userRole = roles.findByName("USER").orElseGet(() -> roles.save(new Role("USER")));

        User u = new User();
        u.setUsername(form.getUsername());
        u.setPassword(pe.encode(form.getPassword()));
        u.setEnabled(true);
        u.setRoles(Set.of(userRole));

        users.save(u);
    }
}
