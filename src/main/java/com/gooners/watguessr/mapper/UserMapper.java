package com.gooners.watguessr.mapper;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmailAddress(user.getEmailAddress());
        dto.setElo(user.getElo());
        dto.setStreak(user.getStreak());
        
        return dto;
    }
}
