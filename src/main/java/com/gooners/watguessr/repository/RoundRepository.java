package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Round;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class RoundRepository extends EntityRepository<Round> {
    @PersistenceContext
    private EntityManager entityManager;

    public RoundRepository() {
        super(Round.class);
    }

    public void create(Round round) {
        entityManager.persist(round);
    }

    public void update(Round round) {
        entityManager.merge(round);
    }
}
