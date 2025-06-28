package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SceneRepository extends JpaRepository<Scene, UUID> {

    @Query(value = "SELECT * FROM scene ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Scene getRandom();
}
