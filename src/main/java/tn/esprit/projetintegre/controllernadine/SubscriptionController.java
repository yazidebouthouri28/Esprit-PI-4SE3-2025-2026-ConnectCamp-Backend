package tn.esprit.projetintegre.controllernadine;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.Subscription;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.enums.SubscriptionStatus;
import tn.esprit.projetintegre.servicenadine.SubscriptionService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(@RequestBody User user,
                                                  @RequestParam String planName,
                                                  @RequestParam BigDecimal price,
                                                  @RequestParam int durationDays) {
        return ResponseEntity.ok(subscriptionService.subscribe(
                user, planName, price, durationDays));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Subscription> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancel(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Subscription>> getByStatus(
            @PathVariable SubscriptionStatus status) {
        return ResponseEntity.ok(subscriptionService.getByStatus(status));
    }

    @PostMapping("/renewals/process")
    public ResponseEntity<Void> processRenewals() {
        subscriptionService.processRenewals();
        return ResponseEntity.ok().build();
    }
}