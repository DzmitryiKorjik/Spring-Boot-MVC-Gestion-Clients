package com.example.controller;

import com.example.model.User;
import com.example.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final LoginService loginService;
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new com.example.model.User());
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute("user") User user, RedirectAttributes ra) {
        if (loginService.validateUser(user)) {
            ra.addFlashAttribute("message", "Bienvenue " + user.getUsername() + " !");
            return "redirect:/home";
        } else {
            ra.addFlashAttribute("error", "Nom d'utilisateur ou mot de passe invalide.");
            ra.addFlashAttribute("user", user); // pour pré-remplir l'username
            return "redirect:/login";
        }
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
}
