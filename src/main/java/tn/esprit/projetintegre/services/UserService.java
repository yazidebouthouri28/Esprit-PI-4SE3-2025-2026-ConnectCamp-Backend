package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.UserDTO;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        if (userDetails.getName() != null) user.setName(userDetails.getName());
        if (userDetails.getPhone() != null) user.setPhone(userDetails.getPhone());
        if (userDetails.getAddress() != null) user.setAddress(userDetails.getAddress());
        if (userDetails.getCountry() != null) user.setCountry(userDetails.getCountry());
        if (userDetails.getAge() != null) user.setAge(userDetails.getAge());
        if (userDetails.getAvatar() != null) user.setAvatar(userDetails.getAvatar());
        if (userDetails.getBio() != null) user.setBio(userDetails.getBio());
        if (userDetails.getLocation() != null) user.setLocation(userDetails.getLocation());
        if (userDetails.getWebsite() != null) user.setWebsite(userDetails.getWebsite());
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        user.setIsAdmin(role == Role.ADMIN);
        return userRepository.save(user);
    }

    @Transactional
    public User becomeSeller(Long userId, String storeName, String storeDescription) {
        User user = getUserById(userId);
        user.setIsSeller(true);
        user.setStoreName(storeName);
        user.setStoreDescription(storeDescription);
        user.setSellerSince(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User suspendUser(Long id, String reason) {
        User user = getUserById(id);
        user.setIsSuspended(true);
        user.setSuspensionReason(reason);
        user.setSuspendedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User unsuspendUser(Long id) {
        User user = getUserById(id);
        user.setIsSuspended(false);
        user.setSuspensionReason(null);
        user.setSuspendedAt(null);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public User addLoyaltyPoints(Long userId, int points) {
        User user = getUserById(userId);
        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        updateLoyaltyTier(user);
        return userRepository.save(user);
    }

    private void updateLoyaltyTier(User user) {
        int points = user.getLoyaltyPoints();
        if (points >= 10000) user.setLoyaltyTier("PLATINUM");
        else if (points >= 5000) user.setLoyaltyTier("GOLD");
        else if (points >= 1000) user.setLoyaltyTier("SILVER");
        else user.setLoyaltyTier("BRONZE");
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .country(user.getCountry())
                .age(user.getAge())
                .role(user.getRole())
                .isSeller(user.getIsSeller())
                .isBuyer(user.getIsBuyer())
                .storeName(user.getStoreName())
                .sellerRating(user.getSellerRating())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .isActive(user.getIsActive())
                .loyaltyPoints(user.getLoyaltyPoints())
                .loyaltyTier(user.getLoyaltyTier())
                .experiencePoints(user.getExperiencePoints())
                .level(user.getLevel())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
