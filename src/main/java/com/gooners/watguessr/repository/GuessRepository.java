package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuessRepository extends JpaRepository<Guess, UUID> {
    // JpaRepository provides save(), findById(), findAll(), delete(), etc.
    // No need for custom create() and update() methods

    @Query("SELECT g.user.id, SUM(g.points)                         " +
            "  FROM Guess g                                             " +
            " WHERE g.round.game.id = :gameId                           " +
            " GROUP BY g.user.id                                       " +
            " ORDER BY SUM(g.points) DESC")
    List<Object[]> findUserPointsByGame(@Param("gameId") UUID gameId);

}
