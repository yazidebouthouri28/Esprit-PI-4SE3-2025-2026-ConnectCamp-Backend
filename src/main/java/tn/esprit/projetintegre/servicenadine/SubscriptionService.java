// SubscriptionService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.nadineentities.Subscription;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.enums.SubscriptionStatus;
import tn.esprit.projetintegre.repositorynadine.SubscriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;
    private final InvoiceService invoiceService;

    @Transactional
    public Subscription subscribe(User user, String planName,
                                  BigDecimal price, int durationDays) {
        walletService.withdraw(user.getId(), price, "Abonnement " + planName);

        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .user(user)
                .planName(planName)
                .price(price)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(durationDays))
                .renewalDate(LocalDateTime.now().plusDays(durationDays))
                .autoRenew(true)
                .build());

        invoiceService.generateFromSubscription(sub);
        return sub;
    }

    @Transactional
    public Subscription cancel(Long subscriptionId) {
        Subscription sub = getById(subscriptionId);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setAutoRenew(false);
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void processRenewals() {
        List<Subscription> toRenew = subscriptionRepository
                .findByAutoRenewTrueAndRenewalDateBefore(LocalDateTime.now());

        toRenew.forEach(sub -> {
            try {
                walletService.withdraw(sub.getUser().getId(), sub.getPrice(),
                        "Renouvellement " + sub.getPlanName());
                sub.setStartDate(LocalDateTime.now());
                sub.setEndDate(LocalDateTime.now().plusDays(30));
                sub.setRenewalDate(LocalDateTime.now().plusDays(30));
                subscriptionRepository.save(sub);
                invoiceService.generateFromSubscription(sub);
            } catch (Exception e) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
            }
        });
    }

    public Subscription getById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));
    }

    public List<Subscription> getByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status);
    }
}