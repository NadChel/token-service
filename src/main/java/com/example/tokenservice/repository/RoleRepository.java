package com.example.tokenservice.repository;

import com.example.tokenservice.data.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends Repository<Role, UUID> {
    @EntityGraph(attributePaths = "users")
    Optional<Role> findByAuthority(String authority);

    Role save(Role role);
}
