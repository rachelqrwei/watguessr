package com.gooners.watguessr.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gooners.watguessr.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByEmailAddress(String emailAddress);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR u.username LIKE %:keyword%) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'createdAtAsc' THEN u.createdAt END ASC, " +
            "CASE WHEN :sortBy = 'streakDesc' THEN u.streak END DESC, " +
            "CASE WHEN :sortBy IS NULL OR :sortBy = '' OR :sortBy = 'elo' THEN u.elo END DESC")
    List<User> findSorted(@Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    @Query("SELECT u, " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g WHERE g.winner IS NOT NULL AND g.winner.id = u.id AND g.gameMode = 'Ranked') as gamesWon, " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g JOIN Round r ON r.game.id = g.id JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE g.winner IS NOT NULL AND g.winner.id != u.id AND g.gameMode = 'Ranked' AND guess.user.id = u.id) as gamesLost, " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g JOIN Round r ON r.game.id = g.id JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE guess.user.id = u.id AND g.winner IS NOT NULL) as gamesPlayed " +
           "FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR u.username LIKE %:keyword%) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'gamesPlayedDesc' THEN " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g JOIN Round r ON r.game.id = g.id JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE guess.user.id = u.id AND g.winner IS NOT NULL) END DESC, " +
           "CASE WHEN :sortBy = 'rankedWinsDesc' THEN " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g WHERE g.winner IS NOT NULL AND g.winner.id = u.id AND g.gameMode = 'Ranked') END DESC, " +
           "CASE WHEN :sortBy = 'rankedLossesDesc' THEN " +
           "(SELECT COUNT(DISTINCT g.id) FROM Game g JOIN Round r ON r.game.id = g.id JOIN Guess guess ON guess.round.id = r.id " +
           "WHERE g.winner IS NOT NULL AND g.winner.id != u.id AND g.gameMode = 'Ranked' AND guess.user.id = u.id) END DESC, " +
           "CASE WHEN :sortBy = 'createdAtAsc' THEN u.createdAt END ASC, " +
           "CASE WHEN :sortBy = 'streakDesc' THEN u.streak END DESC, " +
           "CASE WHEN :sortBy IS NULL OR :sortBy = '' OR :sortBy = 'elo' THEN u.elo END DESC")
    List<Object[]> findSortedWithGameStats(@Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    User findByEmailAddress(String to);

    Optional<User> findFirstByEmailAddressAndVerifiedTrue(String emailAddress);

    @Query("SELECT u FROM User u WHERE " +
           "(u.verified = false OR u.verified IS NULL) AND u.createdAt < :cutoffTime")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffTime") OffsetDateTime cutoffTime);

}
