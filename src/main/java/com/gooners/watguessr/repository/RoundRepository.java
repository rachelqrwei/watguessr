package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Round;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods

 
    @Query("SELECT COUNT(r) FROM Round r WHERE r.game.id = :gameId")
    Integer getRoundCountForGame(@Param("gameId") UUID gameId);

    @Query("SELECT g.user.id, SUM(g.points)                         " +
            "  FROM Guess g                                             " +
            " WHERE g.round.game.id = :gameId                           " +
            " GROUP BY g.user.id                                       " +
            " ORDER BY SUM(g.points) DESC")
    List<Object[]> getUserPointsForGame(@Param("gameId") UUID gameId);


    @Query("SELECT SUM(g.points) FROM Guess g " +
            "JOIN g.round r " +
            "WHERE r.game.id = :gameId AND g.user.id = :userId")
    Integer getUserPointsForGameAndUser(@Param("gameId") UUID gameId, @Param("userId") UUID userId);

    @Query("SELECT r FROM Round r " +
           "WHERE r.game.winner IS NOT NULL " +
           "AND r.game.createdAt < :cutoffTime " +
           "AND r.guesses IS NOT EMPTY")
    List<Round> findEmptyRoundsFromFinishedGamesOlderThan(@Param("cutoffTime") OffsetDateTime cutoffTime);
}
