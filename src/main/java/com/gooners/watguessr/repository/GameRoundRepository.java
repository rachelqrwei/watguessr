package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, UUID> {

    @Query("SELECT gr FROM GameRound gr WHERE gr.game.id = :gameId")
    List<GameRound> findByGameId(@Param("gameId") UUID gameId);

    @Query("SELECT COUNT(gr) FROM GameRound gr WHERE gr.game.id = :gameId")
    Integer getRoundCountForGame(@Param("gameId") UUID gameId);
}