package com.example.repository;
import com.example.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository interface for Client entity
public interface ClientRepository extends JpaRepository<Client, Long> {}