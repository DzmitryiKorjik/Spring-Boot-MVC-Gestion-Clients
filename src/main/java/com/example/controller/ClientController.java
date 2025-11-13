package com.example.controller;

import com.example.model.Client;
import com.example.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository repo;
    public ClientController(ClientRepository repo){ this.repo = repo; }

    @GetMapping
    public String list(Model model){
        model.addAttribute("clients", repo.findAll());
        model.addAttribute("client", new Client()); // pour le formulaire d'ajout
        return "clients/list";
    }

    @PostMapping
    public String create(@ModelAttribute("client") Client c, RedirectAttributes ra){
        repo.save(c);
        ra.addFlashAttribute("msg", "Client créé");
        return "redirect:/clients";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model){
        Client c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("client", c);
        return "clients/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("client") Client form, RedirectAttributes ra) {
        Client existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existing.setName(form.getName());
        existing.setEmail(form.getEmail());
        existing.setPhone(form.getPhone());
        existing.setAddress(form.getAddress());
        repo.save(existing);
        ra.addFlashAttribute("msg", "Client mis à jour");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra){
        repo.deleteById(id);
        ra.addFlashAttribute("msg", "Client supprimé");
        return "redirect:/clients";
    }
}
