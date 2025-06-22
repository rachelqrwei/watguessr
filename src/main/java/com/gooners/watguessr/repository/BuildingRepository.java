package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Building;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Repository
@Transactional
public class BuildingRepository extends EntityRepository<Building> {
    @PersistenceContext
    private EntityManager entityManager;

    public BuildingRepository() {
        super(Building.class);
    }
}
