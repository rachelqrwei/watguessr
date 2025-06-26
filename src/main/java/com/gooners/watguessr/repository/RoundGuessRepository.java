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
} 