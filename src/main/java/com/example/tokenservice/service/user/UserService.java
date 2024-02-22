package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.User;

import java.util.Optional;

public interface UserService {
    User save(User user);
    User setDefaultsAndAndSave(User user);
    Optional<User> findByUsername(String username);
}
