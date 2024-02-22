package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.repository.UserRepository;
import com.example.tokenservice.service.role.RoleService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User setDefaultsAndAndSave(User user) {
        addDefaultRoles(user);
        return save(user);
    }

    private void addDefaultRoles(User user) {
        Optional<Role> userRoleOptional = roleService.findByAuthority(Role.USER);
        Role userRole = userRoleOptional.orElse(new Role(Role.USER));
        user.addRole(userRole);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
