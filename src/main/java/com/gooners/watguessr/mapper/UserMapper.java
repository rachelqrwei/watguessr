package com.gooners.watguessr.mapper;

import com.gooners.watguessr.dto.UserCreateDto;
import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

//@Component
//public class UserMapper {
//
//    public UserDto toDto(User user) {
//        if (user == null) {
//            return null;
//        }
//
//        UserDto dto = new UserDto();
//        dto.setId(user.getId());
//        dto.setUsername(user.getUsername());
//        dto.setEmailAddress(user.getEmailAddress());
//        dto.setElo(user.getElo());
//        dto.setStreak(user.getStreak());
//
//        return dto;
//    }
//}
    @Mapper(componentModel = "spring")
    public interface UserMapper {

        UserDto toDto(User user);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "elo",      constant = "150")     // default elo
        @Mapping(target = "streak",   constant = "0")        // default streak
        User toEntity(UserCreateDto dto);
    }

