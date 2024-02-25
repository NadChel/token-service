package com.example.tokenservice.service.role;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BasicRoleServiceTest {
    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    BasicRoleService roleService;

    @Test
    void testFindByAuthority() {
        Role role = new Role(Role.USER);

        given(roleRepository.findByAuthority(Role.USER)).willReturn(Optional.of(role));

        Optional<Role> roleOptional = roleService.findByAuthority(Role.USER);

        assertThat(roleOptional).isPresent();
        assertThat(roleOptional.get()).isEqualTo(role);
    }

    @Test
    void testSave() {
        Role role = new Role(Role.USER);

        Role persistedRole = new Role();
        persistedRole.setAuthority(role.getAuthority());
        persistedRole.setId(UUID.randomUUID());

        given(roleRepository.save(role)).willReturn(persistedRole);

        Role returnedRole = roleService.save(role);

        assertThat(returnedRole).isEqualTo(persistedRole);
    }
}