package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.SceneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("api/scene")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;

    }


    @GetMapping(value = "/image")
    public String getImage(@RequestParam UUID roundId) {
        return sceneService.getImageByRoundId(roundId);
    }

    @GetMapping(value = "/location")
    public HashMap<String, Double> getLocation(@RequestParam UUID roundId) {
        return sceneService.getLocationByRoundId(roundId);
    }
}
