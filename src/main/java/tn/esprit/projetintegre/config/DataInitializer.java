package tn.esprit.projetintegre.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        initializeAdmin();
        log.info("Data initialization completed!");
    }

    private void initializeAdmin() {
        // Check if admin already exists
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            log.info("Admin user already exists. Skipping creation.");
            return;
        }

        // Also check by email to be safe
        if (userRepository.findByEmail("admin@ecommerce.com").isPresent()) {
            log.info("Admin user already exists (by email). Skipping creation.");
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@ecommerce.com");
        admin.setPassword(passwordEncoder.encode("admin123")); // change to a secure password
        admin.setName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);
        admin.setIsAdmin(true);
        admin.setIsSeller(true);
        admin.setIsBuyer(true);
        admin.setSellerVerified(true);
        admin.setEmailVerified(true);
        admin.setLoyaltyTier("BRONZE");
        // Set any other necessary fields

        userRepository.save(admin);
        log.info("Admin user created.");
    }
}