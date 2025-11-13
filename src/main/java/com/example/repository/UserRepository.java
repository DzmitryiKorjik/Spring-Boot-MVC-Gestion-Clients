package com.example.repository;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Repository interface for User entity
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}