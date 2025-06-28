package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Scene;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class SceneRepository extends EntityRepository<Scene> {
    @PersistenceContext
    private EntityManager entityManager;

    public SceneRepository() {
        super(Scene.class);
    }

    public Scene getRandom() {
        return (entityManager
                .createQuery("SELECT s FROM Scene s ORDER BY FUNCTION('RANDOM')", Scene.class)
                .setMaxResults(1)
                .getSingleResult()
        );
    }
}
