package com.example.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

// Représente un utilisateur dans le système avec des rôles associés
@Entity
@Table(name = "users")
public class User {

    // Clé primaire auto-générée
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom d'utilisateur unique
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // 200 pour laisser passer les hash BCrypt (~60 chars)
    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    // Relation plusieurs-à-plusieurs avec les rôles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
