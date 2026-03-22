package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionNumber;
    private BigDecimal amount;
    private TransactionType type;
    private PaymentStatus status;
    private String description;
    private String referenceType;
    private Long referenceId;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Long walletId;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}
