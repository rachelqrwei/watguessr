package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByEmailAddress(String emailAddress);

    // Leaderboard with keyword search and sorting
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR u.username LIKE %:keyword%) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'createdAtAsc' THEN u.createdAt END ASC, " +
            "CASE WHEN :sortBy = 'createdAtDesc' THEN u.createdAt END DESC, " +
            "CASE WHEN :sortBy IS NULL OR :sortBy = '' OR :sortBy = 'elo' THEN u.elo END DESC")
    List<User> findSorted(@Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            Pageable pageable);
}
