// WalletService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.nadineentities.Transaction;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.nadineentities.Wallet;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.repositorynadine.TransactionRepository;
import tn.esprit.projetintegre.repositorynadine.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().user(user).build()));
    }

    public Wallet getByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet introuvable"));
    }

    @Transactional
    public Transaction deposit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(amount));
        wallet.setTotalDeposited(wallet.getTotalDeposited().add(amount));
        walletRepository.save(wallet);

        return transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(wallet.getBalance())
                .status(PaymentStatus.COMPLETED)
                .description(description)
                .processedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public Transaction withdraw(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Solde insuffisant");

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.subtract(amount));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        walletRepository.save(wallet);

        return transactionRepository.save(Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.DEBIT)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(wallet.getBalance())
                .status(PaymentStatus.COMPLETED)
                .description(description)
                .processedAt(LocalDateTime.now())
                .build());
    }

    public BigDecimal getBalance(Long userId) {
        return getByUserId(userId).getBalance();
    }
}