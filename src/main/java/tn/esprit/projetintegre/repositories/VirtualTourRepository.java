package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.VirtualTour;

import java.util.List;

@Repository
public interface VirtualTourRepository extends JpaRepository<VirtualTour, Long> {

    @EntityGraph(attributePaths = {"site"})
    List<VirtualTour> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"site"})
    List<VirtualTour> findByIsActive(Boolean isActive);

    @EntityGraph(attributePaths = {"site"})
    Page<VirtualTour> findByIsActive(Boolean isActive, Pageable pageable);

    @EntityGraph(attributePaths = {"site"})
    List<VirtualTour> findByIsFeatured(Boolean isFeatured);
}