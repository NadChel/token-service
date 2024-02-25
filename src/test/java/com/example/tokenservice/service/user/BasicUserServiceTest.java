package com.example.tokenservice.service.user;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.repository.UserRepository;
import com.example.tokenservice.service.role.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    RoleService roleService;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    BasicUserService userService;

    @Test
    void testSave_withOccupiedUsername() {
        String occupiedUsername = "some_occupied_username";
        given(userRepository.existsByUsername(occupiedUsername)).willReturn(true);

        User user = new User();
        user.setUsername(occupiedUsername);

        assertThatThrownBy(() -> userService.save(user)).isInstanceOf(DuplicateKeyException.class);
        then(userRepository.save(user)).shouldHaveNoInteractions();
    }

    @Test
    void testSave_withVacantUsername() {
        String someVacantUsername = "some_vacant_username";
        given(userRepository.existsByUsername(someVacantUsername)).willReturn(false);

        User userToSave = new User();
        userToSave.setUsername(someVacantUsername);

        User persistedUser = new User();
        persistedUser.setUsername(userToSave.getUsername());
        persistedUser.setId(UUID.randomUUID());

        given(userRepository.save(userToSave)).willReturn(persistedUser);

        User returnedUser = userService.save(userToSave);

        then(userRepository).should().save(userToSave);

        assertThat(returnedUser).isEqualTo(persistedUser);
    }

    @Test
    void encodePassword() {
        String password = "password", username = "donald_d";
        User user = new User();
        user.setPassword(password);
        user.setUsername(username);

        String encodedPassword = "drowssap";
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);

        User userWithEncodedPassword = userService.encodePassword(user);

        assertThat(userWithEncodedPassword).extracting(User::getPassword).isEqualTo(encodedPassword);
        assertThat(userWithEncodedPassword).extracting(User::getUsername).isEqualTo(username);
    }

    @Test
    void addDefaultRoles() {
        User user = new User();
        assumeThat(user.getAuthorities()).isNullOrEmpty();

        given(roleService.findByAuthority(Role.USER)).willReturn(Optional.empty());
        given(roleService.save(new Role(Role.USER))).willAnswer(i -> {
            Role persistedRole = new Role(Role.USER);
            persistedRole.setId(UUID.randomUUID());
            return persistedRole;
        });

        User userWithDefaultRoles = userService.addDefaultRoles(user);

        Set<Role> userAuthorities = userWithDefaultRoles.getAuthorities();
        assertThat(userAuthorities).anyMatch(r -> r.getAuthority().equals(Role.USER) && r.getId() != null);
    }
}