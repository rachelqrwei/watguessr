package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {
    // Buildings will be manually populated in the database. No need for additional
    // CRUD operations.
}
