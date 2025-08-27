package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.Scene;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.repository.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@Transactional
public class SceneService {
    private final SceneRepository sceneRepository;
    private final RoundRepository roundRepository;
    private final S3Service s3Service;

    public SceneService(SceneRepository sceneRepository, RoundRepository roundRepository, S3Service s3Service) {
        this.sceneRepository = sceneRepository;
        this.roundRepository = roundRepository;
        this.s3Service = s3Service;
    }

    public Scene findById(UUID id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scene not found with id: " + id));
    }


    public Scene getRandomExcludingGameScenes(UUID gameId) {
        return sceneRepository.getRandomExcludingGameScenes(gameId);
    }

    public ResponseInputStream<GetObjectResponse> getImageByRoundId(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found with id: " + roundId));
        Scene scene = round.getScene();
        String imageKey = scene.getImage();

        return s3Service.getObjectStream(imageKey);
    }
}
