package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Scene;
import com.gooners.watguessr.repository.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SceneService {
    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    public void delete(UUID id) {
        sceneRepository.deleteById(id);
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
}
