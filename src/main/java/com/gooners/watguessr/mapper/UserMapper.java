package com.gooners.watguessr.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserCreateDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "elo",      constant = "1000")     // default elo
    @Mapping(target = "streak",   constant = "0")        // default streak
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserCreateDto dto);
}
