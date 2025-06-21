package com.gooners.watguessr.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class GameRepository extends EntityRepository {
    @PersistenceContext
    private EntityManager entityManager;
}
