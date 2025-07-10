package com.gooners.watguessr.mapper;

import org.mapstruct.Mapper;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.dto.RoundDto;

@Mapper(componentModel = "spring")
public interface RoundMapper {
    RoundDto toDto(Round round);
    Round   toEntity(RoundDto roundDto);
}
