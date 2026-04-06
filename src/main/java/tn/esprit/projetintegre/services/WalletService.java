package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Transaction;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.entities.Wallet;
import tn.esprit.projetintegre.enums.TransactionStatus;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.TransactionRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.repositories.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    private final TransactionService transactionService;

    public Wallet createWallet(Long userId) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Vérifier si un wallet existe déjà pour cet utilisateur
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Wallet already exists for user with id: " + userId);
        }

        // Créer le wallet
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));
    }

    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        BigDecimal balance = wallet.getBalance();
        return balance != null ? balance : BigDecimal.ZERO;
    }

    public Wallet addFunds(Long userId, BigDecimal amount) {
        // Vérifications
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);

        if (!wallet.getIsActive()) {
            throw new IllegalStateException("Wallet is deactivated. Cannot add funds.");
        }

        BigDecimal currentBalance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);

        // Vérification du plafond
        BigDecimal maxBalance = new BigDecimal("10000");
        if (newBalance.compareTo(maxBalance) > 0) {
            throw new IllegalArgumentException("Maximum wallet balance exceeded. Maximum allowed: " + maxBalance);
        }

        // Sauvegarder l'ancien solde avant modification
        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber())
                .user(wallet.getUser())
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod("WALLET")
                .description("Funds added to wallet")
                .createdAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        transactionService.saveTransaction(transaction); // Nouvelle méthode à ajouter

        return wallet;
    }

    public Wallet deductFunds(Long userId, BigDecimal amount) {
        // Vérifications
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);

        if (!wallet.getIsActive()) {
            throw new IllegalStateException("Wallet is deactivated. Cannot deduct funds.");
        }

        BigDecimal currentBalance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Available: " + currentBalance + ", Required: " + amount);
        }

        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = currentBalance.subtract(amount);
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber())
                .user(wallet.getUser())
                .wallet(wallet)
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod("WALLET")
                .description("Funds deducted from wallet")
                .createdAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        transactionService.saveTransaction(transaction);

        return wallet;
    }

    private String generateTransactionNumber() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Wallet deactivateWallet(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setIsActive(false);
        return walletRepository.save(wallet);
    }

    public Wallet activateWallet(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setIsActive(true);
        return walletRepository.save(wallet);
    }
    public void deleteWallet(Long walletId) {
        Wallet wallet = getWalletById(walletId);

        // Vérifier si le solde est à zéro avant suppression
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete wallet with positive balance. Current balance: " + balance + ". Please withdraw funds first.");
        }

        walletRepository.delete(wallet);
    }

    /**
     * Supprimer un wallet par ID utilisateur
     * @param userId ID de l'utilisateur
     */
    public void deleteWalletByUserId(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        deleteWallet(wallet.getId());
    }
}