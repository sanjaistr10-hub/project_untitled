package com.projectuntitled.member;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Every query here takes a communityId: member data is never read across tenants. */
public interface MemberAccountRepository extends JpaRepository<MemberAccount, UUID> {

    Optional<MemberAccount> findByCommunityIdAndEmail(UUID communityId, String email);

    Optional<MemberAccount> findByIdAndCommunityId(UUID id, UUID communityId);

    boolean existsByCommunityIdAndUsernameAndIdNot(UUID communityId, String username, UUID id);

    long countByCommunityId(UUID communityId);

    long countByCommunityIdAndCreatedAtAfter(UUID communityId, Instant after);

    List<MemberAccount> findTop5ByCommunityIdOrderByCreatedAtDesc(UUID communityId);

    Page<MemberAccount> findByCommunityId(UUID communityId, Pageable pageable);

    /** {@code pattern} must be lower-case, wrapped in %, with %, _ and ! escaped using '!'. */
    @Query("select m from MemberAccount m where m.communityId = :communityId and ("
            + "lower(m.email) like :pattern escape '!' "
            + "or lower(coalesce(m.fullName, '')) like :pattern escape '!' "
            + "or lower(coalesce(m.username, '')) like :pattern escape '!')")
    Page<MemberAccount> search(@Param("communityId") UUID communityId, @Param("pattern") String pattern, Pageable pageable);
}
