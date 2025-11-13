package com.example.controller;

import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.model.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Contrôleur pour la gestion des utilisateurs
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository users;
    private final UserService userService;

    public UserController(UserRepository users, UserService userService) {
        this.users = users;
        this.userService = userService;
    }

    // Liste des utilisateurs (optionnel)
    @GetMapping
    public String list(Model model){
        model.addAttribute("users", users.findAll());
        return "users/list"; // crée la vue si tu veux une liste
    }

    // Formulaire de création
    @GetMapping("/new")
    public String newUser(Model model){
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "users/register";
    }

    // Création (POST)
    @PostMapping
    public String create(@Valid @ModelAttribute("form") RegisterForm form,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/users/new";
        }
        try {
            userService.createUser(form);
            ra.addFlashAttribute("msg", "Utilisateur créé");
            return "redirect:/users";
        } catch (IllegalArgumentException ex) {
            if ("username_exists".equals(ex.getMessage())) {
                br.rejectValue("username", "username.exists", "Nom d'utilisateur déjà pris");
            } else if ("password_mismatch".equals(ex.getMessage())) {
                br.rejectValue("confirmPassword", "password.mismatch", "Les mots de passe ne correspondent pas");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/users/new";
        }
    }
}
