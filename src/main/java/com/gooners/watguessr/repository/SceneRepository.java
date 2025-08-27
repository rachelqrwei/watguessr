package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SceneRepository extends JpaRepository<Scene, UUID> {

    
    @Query(value = """
        SELECT * FROM watguessr.scene s 
        WHERE s.id NOT IN (
            SELECT r.scene_id 
            FROM watguessr.round r 
            WHERE r.game_id = :gameId AND r.scene_id IS NOT NULL
        ) 
        ORDER BY RANDOM() 
        LIMIT 1
        """, nativeQuery = true)
    Scene getRandomExcludingGameScenes(@Param("gameId") UUID gameId);
}
