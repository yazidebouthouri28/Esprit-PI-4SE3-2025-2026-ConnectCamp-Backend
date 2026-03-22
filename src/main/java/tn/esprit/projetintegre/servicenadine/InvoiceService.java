// InvoiceService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.nadineentities.Invoice;
import tn.esprit.projetintegre.nadineentities.Order;
import tn.esprit.projetintegre.nadineentities.Subscription;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.repositorynadine.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateFromOrder(Order order) {
        return invoiceRepository.save(Invoice.builder()
                .user(order.getUser())
                .order(order)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build());
    }

    @Transactional
    public Invoice generateFromSubscription(Subscription subscription) {
        return invoiceRepository.save(Invoice.builder()
                .user(subscription.getUser())
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .subtotal(subscription.getPrice())
                .totalAmount(subscription.getPrice())
                .status(PaymentStatus.PENDING)
                .build());
    }

    @Transactional
    public Invoice markAsPaid(Long invoiceId) {
        Invoice invoice = getById(invoiceId);
        invoice.setStatus(PaymentStatus.COMPLETED);
        invoice.setPaidDate(LocalDate.now());
        invoice.setPaidAmount(invoice.getTotalAmount());
        return invoiceRepository.save(invoice);
    }

    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable"));
    }

    public List<Invoice> getByUser(Long userId) {
        return invoiceRepository.findByUserId(userId);
    }

    public Invoice getByOrder(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Facture introuvable"));
    }
}