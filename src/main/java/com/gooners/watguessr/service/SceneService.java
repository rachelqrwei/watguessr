package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Scene;
import com.gooners.watguessr.repository.SceneRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
        this.sceneRepository.delete(id);
    }

    public Scene findById(UUID id) {
        return this.sceneRepository.find(id);
    }

    public List<Scene> findAll() {
        return this.sceneRepository.findAll();
    }

    public Scene getRandom() {
        return this.sceneRepository.getRandom();
    }
}
