// WalletController.java
package tn.esprit.projetintegre.controllernadine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.nadineentities.Transaction;
import tn.esprit.projetintegre.nadineentities.Wallet;
import tn.esprit.projetintegre.servicenadine.WalletService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getByUserId(userId));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<Transaction> deposit(@PathVariable Long userId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam String description) {
        return ResponseEntity.ok(walletService.deposit(userId, amount, description));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<Transaction> withdraw(@PathVariable Long userId,
                                                @RequestParam BigDecimal amount,
                                                @RequestParam String description) {
        return ResponseEntity.ok(walletService.withdraw(userId, amount, description));
    }
}