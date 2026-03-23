package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.enums.ServiceType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampingServiceRepository extends JpaRepository<CampingService, Long> {

    @EntityGraph(attributePaths = { "site", "provider" })
    @Override
    @org.springframework.lang.NonNull
    Optional<CampingService> findById(@org.springframework.lang.NonNull Long id);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = { "site", "provider" })
    List<CampingService> findByType(ServiceType type);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findByProviderId(Long providerId, Pageable pageable);
}