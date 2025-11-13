package com.example.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Valeurs attendues: "ADMIN", "USER", etc.
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Role() {}
    public Role(String name) { this.name = name; }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // utile pour Set<Role>
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }
    @Override public int hashCode() { return Objects.hash(name); }
}
