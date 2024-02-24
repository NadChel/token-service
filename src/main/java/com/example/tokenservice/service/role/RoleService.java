package com.example.tokenservice.service.role;

import com.example.tokenservice.data.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByAuthority(String authority);
    Role save(Role authority);
}
