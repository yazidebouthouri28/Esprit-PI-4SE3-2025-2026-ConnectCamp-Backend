package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.enums.enums.PaymentStatus;
import tn.esprit.projetintegre.nadineentities.Invoice;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    @EntityGraph(attributePaths = {"user", "order", "subscription"})
    List<Invoice> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "subscription"})
    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Invoice> findByStatus(PaymentStatus status);

}
