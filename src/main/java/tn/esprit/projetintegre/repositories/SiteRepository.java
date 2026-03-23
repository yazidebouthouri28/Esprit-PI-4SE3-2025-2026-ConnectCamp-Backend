package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner"})
    Optional<Site> findById(Long id);

    @EntityGraph(attributePaths = {"owner"})
    Page<Site> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<Site> findByOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    List<Site> findByCity(String city);

    @EntityGraph(attributePaths = {"owner"})
    List<Site> findByType(String type);

    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT s FROM Site s WHERE s.isActive = true AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.city) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Site> searchSites(String keyword, Pageable pageable);
}