package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods
}
