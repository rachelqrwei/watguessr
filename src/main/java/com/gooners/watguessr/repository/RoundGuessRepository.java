package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.RoundGuess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundGuessRepository extends JpaRepository<RoundGuess, UUID> {

    @Query("SELECT rg FROM RoundGuess rg " +
            "JOIN rg.round r " +
            "WHERE r.game.id = :gameId")
    List<RoundGuess> findByGameId(@Param("gameId") UUID gameId);

    @Query("SELECT rg.user.id, SUM(rg.points) FROM RoundGuess rg " +
            "JOIN rg.round r " +
            "WHERE r.game.id = :gameId " +
            "GROUP BY rg.user.id " +
            "ORDER BY SUM(rg.points) DESC")
    List<Object[]> getUserPointsForGame(@Param("gameId") UUID gameId);

    @Query("SELECT SUM(rg.points) FROM RoundGuess rg " +
            "JOIN rg.round r " +
            "WHERE r.game.id = :gameId AND rg.user.id = :userId")
    Integer getUserPointsForGameAndUser(@Param("gameId") UUID gameId, @Param("userId") UUID userId);
}