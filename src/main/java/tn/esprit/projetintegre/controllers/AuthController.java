package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.AuthRequest;
import tn.esprit.projetintegre.dto.AuthResponse;
import tn.esprit.projetintegre.dto.RegisterRequest;
import tn.esprit.projetintegre.services.AuthService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
@Slf4j
public class AuthController {

    private final AuthService authService;

    // Support both /auth and /api/auth paths
    @PostMapping({"/auth/register", "/api/auth/register"})
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        log.info("Registration successful for username: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping({"/auth/login", "/api/auth/login"})
    @Operation(summary = "Login with username/email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login request received for: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        log.info("Login successful for: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping({"/auth/health", "/api/auth/health"})
    @Operation(summary = "Health check for auth service")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is running", "OK"));
    }
}
