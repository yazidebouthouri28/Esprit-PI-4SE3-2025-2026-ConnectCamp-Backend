// CouponRepository.java
package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.nadineentities.Coupon;import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @EntityGraph(attributePaths = {"applicableCategory"})
    Optional<Coupon> findByCode(String code);

    @EntityGraph(attributePaths = {"applicableCategory"})
    List<Coupon> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"applicableCategory"})
    List<Coupon> findByValidUntilAfter(LocalDateTime date);
}