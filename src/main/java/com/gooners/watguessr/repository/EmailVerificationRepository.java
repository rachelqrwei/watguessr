package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    List<EmailVerification> findByCode(String code);

    @Query("SELECT ev FROM EmailVerification ev " +
            "WHERE ev.code = :code")
    EmailVerification findEmailVerificationByCode(@Param("code") String code);

    @Query("""
              select e from EmailVerification e
              where e.user.emailAddress = :email and e.verified = false
              order by e.expiry desc
            """)
    Optional<EmailVerification> findFirstUnverifiedByEmail(@Param("email") String email);

    @Query("""
              select e from EmailVerification e
              where e.user.emailAddress = :email and e.verified = true
              order by e.expiry desc
            """)
    Optional<EmailVerification> findFirstVerifiedByEmail(@Param("email") String email);

    @Query("""
              select e from EmailVerification e
              where e.user.emailAddress = :email 
            """)
    EmailVerificationRepository findByEmail(@Param("email") String email);
}
