package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.Guess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods

    @Query("SELECT r FROM Round r WHERE r.game.id = :gameId")
    List<Round> findByGameId(@Param("gameId") UUID gameId);

    @Query("SELECT COUNT(r) FROM Round r WHERE r.game.id = :gameId")
    Integer getRoundCountForGame(@Param("gameId") UUID gameId);
}
