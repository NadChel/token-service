package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.repository.UserRepository;
import com.example.tokenservice.service.role.RoleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.encoder = encoder;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User encodePassword(User user) {
        String encodedPassword = encoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public User addDefaultRoles(User user) {
        Optional<Role> userRoleOptional = roleService.findByAuthority(Role.USER);
        Role userRole = userRoleOptional.orElse(new Role(Role.USER));
        userRole.addUser(user);
        user.addRole(userRole);
        return user;
    }
}
