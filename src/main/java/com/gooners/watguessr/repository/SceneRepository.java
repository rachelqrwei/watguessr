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
public class SceneRepository extends EntityRepository<Scene, UUID> {
    @PersistenceContext
    private EntityManager entityManager;

    protected SceneRepository(Class entityClass) {
        super(entityClass);
    }
}
