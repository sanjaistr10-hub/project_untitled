package com.projectuntitled.auth.otp;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    Optional<OtpChallenge> findFirstByScopeAndEmailOrderByCreatedAtDesc(String scope, String email);

    Optional<OtpChallenge> findFirstByScopeAndEmailAndConsumedAtIsNullOrderByCreatedAtDesc(String scope, String email);

    long countByScopeAndEmailAndCreatedAtAfter(String scope, String email, Instant after);

    @Modifying
    @Query("update OtpChallenge o set o.consumedAt = :now "
            + "where o.scope = :scope and o.email = :email and o.consumedAt is null")
    int invalidateActive(@Param("scope") String scope, @Param("email") String email, @Param("now") Instant now);
}
