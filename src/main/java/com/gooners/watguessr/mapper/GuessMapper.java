// com/gooners/watguessr/mapper/GuessMapper.java
package com.gooners.watguessr.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.dto.GuessCreateDto;

@Mapper(componentModel = "spring")
public interface GuessMapper {

    @Mapping(source = "user.id",      target = "userId")
    @Mapping(source = "building.id",  target = "buildingId")
    @Mapping(source = "round.id",     target = "roundId")
    GuessDto toDto(Guess guess);

    @Mapping(source = "userId",       target = "user.id")
    @Mapping(source = "buildingId",   target = "building.id")
    @Mapping(source = "roundId",      target = "round.id")
    Guess toEntity(GuessCreateDto dto);
}
