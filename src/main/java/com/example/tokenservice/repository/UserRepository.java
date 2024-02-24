package com.example.tokenservice.repository;

import com.example.tokenservice.data.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {
    @EntityGraph(attributePaths = "authorities")
    User save(User user);
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
