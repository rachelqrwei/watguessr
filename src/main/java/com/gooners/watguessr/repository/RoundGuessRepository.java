package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.RoundGuess;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class RoundGuessRepository extends EntityRepository<RoundGuess> {
    @PersistenceContext
    private EntityManager entityManager;

    public RoundGuessRepository() {
        super(RoundGuess.class);
    }

    public void create(RoundGuess roundGuess) {
        entityManager.persist(roundGuess);
    }

    public void update(RoundGuess roundGuess) {
        entityManager.merge(roundGuess);
    }

    public List<RoundGuess> findByGameId(UUID gameId) {
        return entityManager
                .createQuery("SELECT rg FROM RoundGuess rg " +
                           "JOIN rg.round r " +
                           "JOIN GameRound gr ON gr.round.id = r.id " +
                           "WHERE gr.game.id = :gameId", RoundGuess.class)
                .setParameter("gameId", gameId)
                .getResultList();
    }

    public List<Object[]> getUserPointsForGame(UUID gameId) {
        return entityManager
                .createQuery("SELECT rg.user.id, SUM(rg.points) FROM RoundGuess rg " +
                           "JOIN rg.round r " +
                           "JOIN GameRound gr ON gr.round.id = r.id " +
                           "WHERE gr.game.id = :gameId " +
                           "GROUP BY rg.user.id " +
                           "ORDER BY SUM(rg.points) DESC")
                .setParameter("gameId", gameId)
                .getResultList();
    }
} 