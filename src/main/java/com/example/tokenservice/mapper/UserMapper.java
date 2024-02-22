package com.example.tokenservice.mapper;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDto userDto);
}
