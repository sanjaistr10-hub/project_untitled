package com.projectuntitled.auth.token;

import com.projectuntitled.auth.Audience;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now "
            + "where t.audience = :audience and t.accountId = :accountId and t.revokedAt is null")
    int revokeAllActive(@Param("audience") Audience audience, @Param("accountId") UUID accountId,
            @Param("now") Instant now);
}
