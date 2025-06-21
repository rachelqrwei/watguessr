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
public class RoundRepository extends EntityRepository<Round, UUID> {
    @PersistenceContext
    private EntityManager entityManager;

    protected RoundRepository(Class<Round> entityClass) {
        super(entityClass);
    }
}
