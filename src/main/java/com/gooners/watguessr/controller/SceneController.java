package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.SceneService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.UUID;

@RestController
@RequestMapping("api/scene")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;

    }

    @GetMapping(value = "/image")
    public ResponseEntity<byte[]> getImage(@RequestParam UUID roundId) {
        // TODO: ??????.
        try (ResponseInputStream<GetObjectResponse> objectStream = sceneService.getImageByRoundId(roundId)) {
            String contentType = objectStream.response().contentType() != null
                    ? objectStream.response().contentType()
                    : "image/jpeg";
            byte[] bytes = objectStream.readAllBytes();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
