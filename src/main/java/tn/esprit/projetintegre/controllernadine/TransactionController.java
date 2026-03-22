// TransactionController.java
package tn.esprit.projetintegre.controllernadine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.Transaction;
import tn.esprit.projetintegre.servicenadine.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<Transaction>> getByWallet(@PathVariable Long walletId) {
        return ResponseEntity.ok(transactionService.getByWallet(walletId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getByUser(userId));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Transaction> getByNumber(@PathVariable String number) {
        return ResponseEntity.ok(transactionService.getByNumber(number));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPending() {
        return ResponseEntity.ok(transactionService.getPending());
    }
}