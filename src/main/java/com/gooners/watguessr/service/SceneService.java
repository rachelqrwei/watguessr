package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.Scene;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.repository.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SceneService {
    private final SceneRepository sceneRepository;
    private final RoundRepository roundRepository;

    public SceneService(SceneRepository sceneRepository, RoundRepository roundRepository) {
        this.sceneRepository = sceneRepository;
        this.roundRepository = roundRepository;
    }

    public Scene findById(UUID id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scene not found with id: " + id));
    }

    public List<Scene> findAll() {
        return sceneRepository.findAll();
    }

    public Scene getRandom() {
        return sceneRepository.getRandom();
    }

    public String getImageByRoundId(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found with id: " + roundId));
        Scene scene = round.getScene();
        return scene.getImage();
    }

    public HashMap<String, Double> getLocationByRoundId(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found with id: " + roundId));
        Scene scene = round.getScene();
        HashMap<String, Double> location = new HashMap<>();
        location.put("longitude", scene.getLocationX());
        location.put("latitude", scene.getLocationY());
        return location;
    }
}
