package com.example.tokenservice.mapper;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class UserMapperTest {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);
    @Test
    void testToUser() {
        String username = "mickey_m", password = "pass";
        UserDto userDto = UserDto.builder().username(username).password(password).build();
        User user = mapper.toUser(userDto);
        assertSoftly(soft -> {
           soft.assertThat(user.getUsername()).isEqualTo(username);
           soft.assertThat(user.getPassword()).isEqualTo(password);
        });
    }
}