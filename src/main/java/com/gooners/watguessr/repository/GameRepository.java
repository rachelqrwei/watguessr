package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    
    @Query("SELECT COUNT(DISTINCT g.id) FROM Game g " +
           "JOIN Round r ON r.game.id = g.id " +
           "JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE guess.user.id = :userId")
    Integer countGamesPlayedByUser(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(g.id) FROM Game g " +
           "WHERE g.winner.id = :userId")
    Integer countGamesWonByUser(@Param("userId") UUID userId);
    
}
