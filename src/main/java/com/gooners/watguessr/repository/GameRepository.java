package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    
    @Query("SELECT COUNT(DISTINCT g.id) FROM Game g " +
           "WHERE g.winner IS NOT NULL AND g.winner.id = :userId AND g.gameMode = 'Ranked'")
    Integer countRankedGamesWonByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT g.id) FROM Game g " +
            "WHERE g.winner IS NOT NULL AND g.winner.id != :userId AND g.gameMode = 'Ranked'")
    Integer countRankedGamesLostByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT g.id) FROM Game g " +
           "JOIN Round r ON r.game.id = g.id " +
           "JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE guess.user.id = :userId AND g.winner IS NOT NULL")
    Integer countGamesPlayedByUser(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT g FROM Game g " +
           "JOIN Round r ON r.game.id = g.id " +
           "JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE guess.user.id = :userId " +
           "ORDER BY g.createdAt DESC")
    Page<Game> findGamesByUser(@Param("userId") UUID userId, Pageable pageable);

    List<Game> findByIsPrivateFalseAndGameMode(String gameMode);

    Optional<Game> findByLobbyCode(String lobbyCode);

    List<Game> findByWinnerIsNullAndCreatedAtBefore(OffsetDateTime dateTime);

    // New methods for user deletion
    @Query("SELECT g FROM Game g WHERE g.winner.id = :userId")
    List<Game> findGamesWonByUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Game g SET g.winner = NULL WHERE g.winner.id = :userId")
    void clearWinnerForUser(@Param("userId") UUID userId);

}
