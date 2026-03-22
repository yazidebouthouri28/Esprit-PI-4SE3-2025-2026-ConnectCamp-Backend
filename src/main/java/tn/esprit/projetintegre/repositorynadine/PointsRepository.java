package tn.esprit.projetintegre.repositorynadine;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.nadineentities.Points;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Points> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Points> findByUserIdAndExpiredFalseAndUsedFalse(Long userId);

    @Query("SELECT COALESCE(SUM(p.points), 0) FROM Points p " +
            "WHERE p.user.id = :userId " +
            "AND p.expired = false AND p.used = false " +
            "AND (p.expirationDate IS NULL OR p.expirationDate > :now)")
    Integer getTotalAvailablePoints(@Param("userId") Long userId,
                                    @Param("now") LocalDateTime now);
}
