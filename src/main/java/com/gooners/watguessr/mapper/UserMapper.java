package com.gooners.watguessr.mapper;

import com.gooners.watguessr.dto.UserCreateDto;
import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserSettingsDto;
import com.gooners.watguessr.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


    @Mapper(componentModel = "spring")
    public interface UserMapper {

        UserDto toDto(User user);

        UserSettingsDto toUserSettingsDto(User user);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "elo",      constant = "150")     // default elo
        @Mapping(target = "streak",   constant = "0")        // default streak
        User toEntity(UserCreateDto dto);
    }

