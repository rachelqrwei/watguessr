package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.GameRound;
import com.gooners.watguessr.entity.Round;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class GameRoundRepository extends EntityRepository<GameRound> {
    @PersistenceContext
    private EntityManager entityManager;

    public GameRoundRepository() {
        super(GameRound.class);
    }

    public void create(GameRound gameRound) {
        entityManager.persist(gameRound);
    }

    public GameRound findByRound(Round round) {
        return entityManager.find(GameRound.class, round.getId());
    }

    public void update(GameRound gameRound) {
        entityManager.merge(gameRound);
    }
} 