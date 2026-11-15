package com.projectuntitled.community;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, UUID> {

    Optional<Community> findBySlug(String slug);

    Optional<Community> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);
}
