package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.MatchmakingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchmakingQueueRepository extends JpaRepository<MatchmakingQueue, UUID> {

	List<MatchmakingQueue> findByUserIdAndStatus(UUID userId, String status);

	@Query("SELECT mq FROM MatchmakingQueue mq WHERE mq.status = 'waiting' " +
			"AND mq.user.elo BETWEEN :minElo AND :maxElo " +
			"AND mq.createdAt >= :minCreatedAt " +
			"ORDER BY mq.createdAt ASC")
	List<MatchmakingQueue> findCompatibleRankedPlayers(@Param("minElo") int minElo,
	                                                   @Param("maxElo") int maxElo,
	                                                   @Param("minCreatedAt") OffsetDateTime minCreatedAt);

	List<MatchmakingQueue> findByStatus(String status);

	@Query("SELECT mq FROM MatchmakingQueue mq WHERE mq.status = 'waiting' AND mq.createdAt < :cutoff")
	List<MatchmakingQueue> findExpiredEntries(@Param("cutoff") OffsetDateTime cutoff);

}