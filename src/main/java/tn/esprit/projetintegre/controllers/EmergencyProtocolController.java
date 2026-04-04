package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.entities.EmergencyProtocol;
import tn.esprit.projetintegre.services.EmergencyProtocolService;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-protocols")
@RequiredArgsConstructor
@Tag(name = "Emergency Protocols", description = "Emergency Protocol management APIs")
public class EmergencyProtocolController {

    private final EmergencyProtocolService emergencyProtocolService;

    @GetMapping
    @Operation(summary = "Get all active emergency protocols")
    public ResponseEntity<ApiResponse<List<EmergencyProtocol>>> getAllActiveProtocols() {
        List<EmergencyProtocol> protocols = emergencyProtocolService.getAllActiveProtocols();
        return ResponseEntity.ok(ApiResponse.success(protocols));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get emergency protocol by ID")
    public ResponseEntity<ApiResponse<EmergencyProtocol>> getProtocolById(@PathVariable Long id) {
        EmergencyProtocol protocol = emergencyProtocolService.getProtocolById(id);
        return ResponseEntity.ok(ApiResponse.success(protocol));
    }

    @PostMapping
    @Operation(summary = "Create a new emergency protocol (ADMIN only)")
    public ResponseEntity<ApiResponse<EmergencyProtocol>> createProtocol(
            @Valid @RequestBody EmergencyProtocol protocol) {
        EmergencyProtocol created = emergencyProtocolService.createProtocol(protocol);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency protocol created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an emergency protocol (ADMIN only)")
    public ResponseEntity<ApiResponse<EmergencyProtocol>> updateProtocol(
            @PathVariable Long id,
            @Valid @RequestBody EmergencyProtocol protocol) {
        EmergencyProtocol updated = emergencyProtocolService.updateProtocol(id, protocol);
        return ResponseEntity.ok(ApiResponse.success("Emergency protocol updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an emergency protocol (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteProtocol(@PathVariable Long id) {
        emergencyProtocolService.deleteProtocol(id);
        return ResponseEntity.ok(ApiResponse.success("Emergency protocol deleted successfully", null));
    }
}
