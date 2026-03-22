// PointsService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.nadineentities.Points;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.repositorynadine.PointsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;

    @Transactional
    public Points earnPoints(User user, Integer amount, String description,
                             String referenceType, Long referenceId) {
        return pointsRepository.save(Points.builder()
                .user(user)
                .points(amount)
                .transactionType(TransactionType.CREDIT)
                .description(description)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(365))
                .build());
    }

    @Transactional
    public Points redeemPoints(User user, Integer amount, String description) {
        int available = getAvailablePoints(user.getId());
        if (available < amount)
            throw new RuntimeException("Points insuffisants");

        List<Points> pointsList = pointsRepository
                .findByUserIdAndExpiredFalseAndUsedFalse(user.getId());

        int remaining = amount;
        for (Points p : pointsList) {
            if (remaining <= 0) break;
            if (p.getPoints() <= remaining) {
                remaining -= p.getPoints();
                p.setUsed(true);
            } else {
                p.setPoints(p.getPoints() - remaining);
                remaining = 0;
            }
            pointsRepository.save(p);
        }

        return pointsRepository.save(Points.builder()
                .user(user)
                .points(-amount)
                .transactionType(TransactionType.DEBIT)
                .description(description)
                .transactionDate(LocalDateTime.now())
                .used(true)
                .build());
    }

    public Integer getAvailablePoints(Long userId) {
        return pointsRepository.getTotalAvailablePoints(userId, LocalDateTime.now());
    }

    @Transactional
    public void expirePoints() {
        List<Points> expired = pointsRepository.findAll().stream()
                .filter(p -> !p.getExpired() && !p.getUsed()
                        && p.getExpirationDate() != null
                        && p.getExpirationDate().isBefore(LocalDateTime.now()))
                .toList();
        expired.forEach(p -> p.setExpired(true));
        pointsRepository.saveAll(expired);
    }
}