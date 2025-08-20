package com.gooners.watguessr.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.gooners.watguessr.entity.Scene;
import com.gooners.watguessr.dto.SceneDto;

@Mapper(componentModel = "spring")
public interface SceneMapper {
    
    @Mapping(target = "buildingName", source = "building.name")
    SceneDto toDto(Scene scene);
}
