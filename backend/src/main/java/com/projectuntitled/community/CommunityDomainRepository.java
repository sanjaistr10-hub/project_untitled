package com.projectuntitled.community;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityDomainRepository extends JpaRepository<CommunityDomain, UUID> {

    Optional<CommunityDomain> findByHost(String host);
}
