package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GuessRepository extends JpaRepository<Guess, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods
}
