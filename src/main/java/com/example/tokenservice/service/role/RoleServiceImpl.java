package com.example.tokenservice.service.role;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByAuthority(String authority) {
        return roleRepository.findByAuthority(authority);
    }
}
