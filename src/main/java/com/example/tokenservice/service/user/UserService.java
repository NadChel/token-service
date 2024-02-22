package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.User;

public interface UserService {
    User save(User user);

    User encodePassword(User user);

    User addDefaultRoles(User user);
}
