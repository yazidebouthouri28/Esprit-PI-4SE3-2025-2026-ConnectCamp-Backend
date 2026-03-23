package tn.esprit.projetintegre.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        initializeAdmin();
        initializeSampleUsers();
        
        log.info("Data initialization completed!");
    }

    private void initializeAdmin() {
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = User.builder()
                    .name("Administrator")
                    .username("admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isAdmin(true)
                    .isSeller(true)
                    .isBuyer(true)
                    .isActive(true)
                    .isSuspended(false)
                    .emailVerified(true)
                    .phoneVerified(false)
                    .loyaltyPoints(0)
                    .loyaltyTier("BRONZE")
                    .experiencePoints(0)
                    .level(1)
                    .totalMissionsCompleted(0)
                    .sellerReviewCount(0)
                    .totalSales(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .sellerVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin@ecommerce.com / admin123");
        } else {
            // Update existing admin user to ensure isSuspended is not null
            userRepository.findByEmail("admin@ecommerce.com").ifPresent(admin -> {
                if (admin.getIsSuspended() == null) {
                    admin.setIsSuspended(false);
                    admin.setIsActive(true);
                    userRepository.save(admin);
                    log.info("Updated admin user with missing fields");
                }
            });
        }
    }

    private void initializeSampleUsers() {
        // Create a sample seller
        if (!userRepository.existsByEmail("seller@ecommerce.com")) {
            User seller = User.builder()
                    .name("Sample Seller")
                    .username("seller")
                    .email("seller@ecommerce.com")
                    .password(passwordEncoder.encode("seller123"))
                    .role(Role.SELLER)
                    .isSeller(true)
                    .isBuyer(true)
                    .isAdmin(false)
                    .isActive(true)
                    .isSuspended(false)
                    .emailVerified(true)
                    .phoneVerified(false)
                    .loyaltyPoints(0)
                    .loyaltyTier("BRONZE")
                    .experiencePoints(0)
                    .level(1)
                    .totalMissionsCompleted(0)
                    .storeName("Sample Store")
                    .storeDescription("A sample seller store for testing")
                    .sellerVerified(true)
                    .sellerSince(LocalDateTime.now())
                    .sellerReviewCount(0)
                    .totalSales(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .build();
            userRepository.save(seller);
            log.info("Seller user created: seller@ecommerce.com / seller123");
        } else {
            userRepository.findByEmail("seller@ecommerce.com").ifPresent(seller -> {
                if (seller.getIsSuspended() == null) {
                    seller.setIsSuspended(false);
                    seller.setIsActive(true);
                    userRepository.save(seller);
                    log.info("Updated seller user with missing fields");
                }
            });
        }

        // Create a sample buyer
        if (!userRepository.existsByEmail("buyer@ecommerce.com")) {
            User buyer = User.builder()
                    .name("Sample Buyer")
                    .username("buyer")
                    .email("buyer@ecommerce.com")
                    .password(passwordEncoder.encode("buyer123"))
                    .role(Role.BUYER)
                    .isSeller(false)
                    .isBuyer(true)
                    .isAdmin(false)
                    .isActive(true)
                    .isSuspended(false)
                    .emailVerified(true)
                    .phoneVerified(false)
                    .loyaltyPoints(0)
                    .loyaltyTier("BRONZE")
                    .experiencePoints(0)
                    .level(1)
                    .totalMissionsCompleted(0)
                    .build();
            userRepository.save(buyer);
            log.info("Buyer user created: buyer@ecommerce.com / buyer123");
        } else {
            userRepository.findByEmail("buyer@ecommerce.com").ifPresent(buyer -> {
                if (buyer.getIsSuspended() == null) {
                    buyer.setIsSuspended(false);
                    buyer.setIsActive(true);
                    userRepository.save(buyer);
                    log.info("Updated buyer user with missing fields");
                }
            });
        }
    }
}
