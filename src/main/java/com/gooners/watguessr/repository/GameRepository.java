package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Game;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class GameRepository extends EntityRepository<Game> {
    @PersistenceContext
    private EntityManager entityManager;

    public GameRepository() {
        super(Game.class);
    }
}
