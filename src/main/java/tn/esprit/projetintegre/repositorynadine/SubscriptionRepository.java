package tn.esprit.projetintegre.repositorynadine;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.esprit.projetintegre.enums.SubscriptionStatus;
import tn.esprit.projetintegre.nadineentities.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<Subscription> findByStatus(SubscriptionStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<Subscription> findByAutoRenewTrueAndRenewalDateBefore(LocalDateTime date);
}