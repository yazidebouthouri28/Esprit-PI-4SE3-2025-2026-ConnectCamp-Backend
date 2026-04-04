package tn.esprit.projetintegre.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.esprit.projetintegre.enums.Role;

public class SecurityUtil {
    /**
     * Checks if the currently authenticated user has the given role.
     */
    public static boolean hasRole(Role requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_" + requiredRole.name()));
    }
}
