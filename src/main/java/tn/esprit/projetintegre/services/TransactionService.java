package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Transaction;
import tn.esprit.projetintegre.entities.Wallet;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.TransactionRepository;
import tn.esprit.projetintegre.repositories.WalletRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public Transaction getTransactionByNumber(String transactionNumber) {
        return transactionRepository.findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with number: " + transactionNumber));
    }

    public Page<Transaction> getTransactionsByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    public Page<Transaction> getTransactionsByWalletId(Long walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable);
    }

    public Page<Transaction> getTransactionsByType(TransactionType type, Pageable pageable) {
        return transactionRepository.findByType(type, pageable);
    }

    public Transaction createTransaction(Transaction transaction, Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        BigDecimal balanceBefore = wallet.getBalance();
        transaction.setWallet(wallet);
        transaction.setUser(wallet.getUser());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setStatus(PaymentStatus.COMPLETED);

        // Update wallet balance based on transaction type
        if (transaction.getType() == TransactionType.DEPOSIT || 
            transaction.getType() == TransactionType.REFUND) {
            wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        } else {
            if (wallet.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
        }

        transaction.setBalanceAfter(wallet.getBalance());
        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }
}
