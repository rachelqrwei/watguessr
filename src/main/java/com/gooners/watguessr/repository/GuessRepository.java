package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuessRepository extends JpaRepository<Guess, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods

    Optional<Guess> findFirstByRoundIdAndUserId(UUID roundId, UUID userId);

    @Query("SELECT g.user.id, SUM(g.points)                         " +
            "  FROM Guess g                                             " +
            " WHERE g.round.game.id = :gameId                           " +
            " GROUP BY g.user.id                                       " +
            " ORDER BY SUM(g.points) DESC")
    List<Object[]> findUserPointsByGame(@Param("gameId") UUID gameId);

    // New methods for user deletion
    @Query("SELECT g FROM Guess g WHERE g.user.id = :userId")
    List<Guess> findAllByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Guess g WHERE g.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

}
