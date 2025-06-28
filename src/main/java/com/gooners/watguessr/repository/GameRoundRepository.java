package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.GameRound;
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

    public void update(GameRound gameRound) {
        entityManager.merge(gameRound);
    }

    public List<GameRound> findByGameId(UUID gameId) {
        return entityManager
                .createQuery("SELECT gr FROM GameRound gr WHERE gr.game.id = :gameId", GameRound.class)
                .setParameter("gameId", gameId)
                .getResultList();
    }

    public Integer getRoundCountForGame(UUID gameId) {
        return Math.toIntExact((Long) entityManager
                .createQuery("SELECT COUNT(gr) FROM GameRound gr WHERE gr.game.id = :gameId")
                .setParameter("gameId", gameId)
                .getSingleResult());
    }
} 