package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Guess;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class GuessRepository extends EntityRepository<Guess> {
    @PersistenceContext
    private EntityManager entityManager;

    public GuessRepository() {
        super(Guess.class);
    }

    public void create(Guess guess) {
        entityManager.persist(guess);
    }

    public void update(Guess guess) {
        entityManager.merge(guess);
    }
}
