package tn.esprit.projetintegre.controllernadine;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.RefundRequest;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.enums.RefundRequestType;
import tn.esprit.projetintegre.servicenadine.RefundRequestService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundRequestController {

    private final RefundRequestService refundRequestService;

    @PostMapping("/submit")
    public ResponseEntity<RefundRequest> submit(@RequestBody User user,
                                                @RequestParam Long orderId,
                                                @RequestParam BigDecimal amount,
                                                @RequestParam String reason,
                                                @RequestParam RefundRequestType type) {
        return ResponseEntity.ok(refundRequestService.submit(
                user, orderId, amount, reason, type));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RefundRequest>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(refundRequestService.getByUser(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RefundRequest>> getPending() {
        return ResponseEntity.ok(refundRequestService.getPending());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RefundRequest> approve(@PathVariable Long id,
                                                 @RequestBody User admin,
                                                 @RequestParam BigDecimal approvedAmount) {
        return ResponseEntity.ok(refundRequestService.approve(id, admin, approvedAmount));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<RefundRequest> reject(@PathVariable Long id,
                                                @RequestBody User admin,
                                                @RequestParam String reason) {
        return ResponseEntity.ok(refundRequestService.reject(id, admin, reason));
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<RefundRequest> process(@PathVariable Long id) {
        return ResponseEntity.ok(refundRequestService.process(id));
    }
}