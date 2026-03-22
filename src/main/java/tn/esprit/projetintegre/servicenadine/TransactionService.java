// TransactionService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.nadineentities.Transaction;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.repositorynadine.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getByWallet(Long walletId) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    public List<Transaction> getByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getPending() {
        return transactionRepository.findByStatus(PaymentStatus.PENDING);
    }

    public Transaction getByNumber(String number) {
        return transactionRepository.findByTransactionNumber(number)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable"));
    }
}