package com.example.service;

import org.springframework.stereotype.Service;
import com.example.model.User;
import com.example.repository.UserRepository;

// Service pour la gestion de la connexion des utilisateurs
@Service
public class LoginService {

    private final UserRepository userRepository;

    // Injection par constructeur (recommandé)
    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Méthode pour valider les informations de connexion d'un utilisateur
    public boolean validateUser(User user) {
        String u = user.getUsername() == null ? "" : user.getUsername().trim();
        String p = user.getPassword() == null ? "" : user.getPassword().trim();

        return userRepository.findByUsername(u)
                .map(stored -> stored.getPassword() != null && stored.getPassword().equals(p))
                .orElse(false);
    }
}
