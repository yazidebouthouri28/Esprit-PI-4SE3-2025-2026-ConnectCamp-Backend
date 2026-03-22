package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.enums.enums.RefundStatus;
import tn.esprit.projetintegre.nadineentities.RefundRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest,Long> {
    @EntityGraph(attributePaths = {"order", "reviewedBy"})
    List<RefundRequest> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "order", "reviewedBy"})
    List<RefundRequest> findByStatus(RefundStatus status);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    Optional<RefundRequest> findByOrderId(Long orderId);

    Optional<RefundRequest> findByRequestNumber(String requestNumber);

}
