// RefundRequestService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.nadineentities.Order;
import tn.esprit.projetintegre.nadineentities.RefundRequest;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.enums.RefundRequestType;
import tn.esprit.projetintegre.enums.RefundStatus;
import tn.esprit.projetintegre.repositorynadine.RefundRequestRepository;
import tn.esprit.projetintegre.services.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundRequestService {

    private final RefundRequestRepository refundRequestRepository;
    private final WalletService walletService;
    private final OrderService orderService;

    @Transactional
    public RefundRequest submit(User user, Long orderId, BigDecimal amount,
                                String reason, RefundRequestType type) {
        Order order = orderService.getById(orderId);
        return refundRequestRepository.save(RefundRequest.builder()
                .user(user)
                .order(order)
                .requestedAmount(amount)
                .reason(reason)
                .requestType(type)
                .status(RefundStatus.PENDING)
                .build());
    }

    @Transactional
    public RefundRequest approve(Long requestId, User admin,
                                 BigDecimal approvedAmount) {
        RefundRequest request = getById(requestId);
        request.setStatus(RefundStatus.APPROVED);
        request.setApprovedAmount(approvedAmount);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        return refundRequestRepository.save(request);
    }

    @Transactional
    public RefundRequest reject(Long requestId, User admin, String reason) {
        RefundRequest request = getById(requestId);
        request.setStatus(RefundStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        return refundRequestRepository.save(request);
    }

    @Transactional
    public RefundRequest process(Long requestId) {
        RefundRequest request = getById(requestId);
        if (request.getStatus() != RefundStatus.APPROVED)
            throw new RuntimeException("La demande doit être approuvée d'abord");

        walletService.deposit(request.getUser().getId(),
                request.getApprovedAmount(),
                "Remboursement commande "
                        + request.getOrder().getOrderNumber());

        request.setStatus(RefundStatus.PROCESSED);
        request.setProcessedAt(LocalDateTime.now());
        return refundRequestRepository.save(request);
    }

    public RefundRequest getById(Long id) {
        return refundRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
    }

    public List<RefundRequest> getByUser(Long userId) {
        return refundRequestRepository.findByUserId(userId);
    }

    public List<RefundRequest> getPending() {
        return refundRequestRepository.findByStatus(RefundStatus.PENDING);
    }
}