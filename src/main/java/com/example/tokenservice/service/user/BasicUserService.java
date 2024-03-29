package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.repository.UserRepository;
import com.example.tokenservice.service.role.RoleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder encoder;

    public BasicUserService(UserRepository userRepository,
                            RoleService roleService,
                            PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.encoder = encoder;
    }

    @Override
    public User save(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new DuplicateKeyException("Username already taken: " + user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public User encodePassword(User user) {
        String encodedPassword = encoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    @Transactional
    public User addDefaultRoles(User user) {
        Optional<Role> userRoleOptional = roleService.findByAuthority(Role.USER);
        Role userRole = userRoleOptional.orElseGet(() -> roleService.save(new Role(Role.USER)));
        userRole.addUser(user);
        user.addRole(userRole);
        return user;
    }
}
