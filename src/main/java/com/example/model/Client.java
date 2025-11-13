package com.example.model;

import jakarta.persistence.*;

// Client entity representing a client in the system
@Entity
@Table(name = "clients")
public class Client {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Client name
    @Column(nullable = false, length = 150)
    private String name;

    // Client email
    @Column(length = 150)
    private String email;

    // Client phone number
    @Column(length = 50)
    private String phone;

    // Client address
    @Column(length = 255)
    private String address;

    public Client() {}

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
