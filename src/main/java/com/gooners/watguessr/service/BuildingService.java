package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Building;
import com.gooners.watguessr.repository.BuildingRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BuildingService {
    private final BuildingRepository buildingRepository;

    @Autowired
    private EntityManager entityManager;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public void create(Building building) {
        entityManager.persist(building);
    }

    public void update(Building building) {

    }

    public void delete(UUID id) {
        this.buildingRepository.delete(id);
    }

    public Building findById(UUID id) {
        return this.buildingRepository.find(id);
    }

    public List<Building> findAll() {
        return this.buildingRepository.findAll();
    }
}
