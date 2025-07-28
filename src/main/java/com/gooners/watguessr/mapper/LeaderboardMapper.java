package com.gooners.watguessr.mapper;

import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaderboardMapper {

    @Mapping(target = "gamesPlayed", constant = "0") 
    @Mapping(target = "gamesWon", constant = "0") 
    @Mapping(target = "gamesLost", constant = "0") 
    LeaderboardUser toLeaderboardUser(User user);
} 