package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Building;
import com.gooners.watguessr.service.BuildingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/building")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping("/all")
    public List<Building> getAllBuildings() {
        return buildingService.findAll();
    }
}
