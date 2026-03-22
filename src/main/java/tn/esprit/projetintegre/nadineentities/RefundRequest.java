package tn.esprit.projetintegre.nadineentities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.RefundRequestType;
import tn.esprit.projetintegre.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "refund_requests", indexes = {
        @Index(name = "idx_refundreq_number", columnList = "requestNumber"),
        @Index(name = "idx_refundreq_user", columnList = "user_id"),
        @Index(name = "idx_refundreq_order", columnList = "order_id"),
        @Index(name = "idx_refundreq_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de demande est obligatoire")
    @Size(max = 50)
    @Column(unique = true)
    private String requestNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le type de demande est obligatoire")
    private RefundRequestType requestType;

    // Montants
    @NotNull(message = "Le montant demandé est obligatoire")
    @DecimalMin(value = "0.01")
    @Column(precision = 10, scale = 2)
    private BigDecimal requestedAmount;

    @DecimalMin(value = "0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    // Raison + description (Remboursement.raison fusionnée ici)
    @NotBlank(message = "La raison est obligatoire")
    @Size(max = 2000)
    @Column(length = 2000)
    private String reason;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    // Ajouté depuis Remboursement
    @Size(max = 100)
    private String refundMethod;      // = methodeRemboursement

    @Size(max = 100)
    private String transactionId;     // = transactionId

    // Preuves photos (depuis RefundRequest)
    @ElementCollection
    @CollectionTable(name = "refund_request_images", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    // Dates du workflow
    private LocalDateTime submittedAt;    // = dateDemande
    private LocalDateTime reviewedAt;
    private LocalDateTime processedAt;    // = dateTraitement

    // Notes admin
    @Size(max = 2000)
    @Column(length = 2000)
    private String adminNotes;            // = notesAdmin

    @Size(max = 1000)
    @Column(length = 1000)
    private String rejectionReason;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;              // = traitePar

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestNumber == null) {
            requestNumber = "RFR-" + System.currentTimeMillis();
        }
        if (submittedAt == null) submittedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}