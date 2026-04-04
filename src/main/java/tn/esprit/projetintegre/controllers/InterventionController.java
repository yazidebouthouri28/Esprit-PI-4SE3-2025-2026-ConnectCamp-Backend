package tn.esprit.projetintegre.controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.EmergencyIntervention;
import tn.esprit.projetintegre.services.EmergencyInterventionService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
public class InterventionController {

    private final EmergencyInterventionService interventionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createIntervention(@RequestBody InterventionRequest request) {
        EmergencyIntervention intervention = interventionService.createIntervention(
                request.getAlerteId(),
                request.getType(),
                request.getDescription(),
                request.getStatus(),
                request.getEquipe()
        );
        return ResponseEntity.ok(mapToResponse(intervention));
    }

    @GetMapping("/alerte/{alertId}")
    public ResponseEntity<List<Map<String, Object>>> getByAlertId(@PathVariable Long alertId) {
        List<EmergencyIntervention> interventions = interventionService.getInterventionsByAlert(alertId);
        List<Map<String, Object>> response = interventions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapToResponse(EmergencyIntervention iv) {
        return Map.of(
            "id", iv.getId(),
            "alerteId", iv.getAlert() != null ? iv.getAlert().getId() : 0L,
            "agentId", iv.getDeployedResources() != null ? iv.getDeployedResources() : "Unknown",
            "description", iv.getDescription() != null ? iv.getDescription() : "",
            "type", iv.getInterventionType() != null ? iv.getInterventionType().name() : "",
            "status", iv.getStatus() != null ? iv.getStatus() : "",
            "startTime", iv.getCreatedAt() != null ? iv.getCreatedAt().toString() : ""
        );
    }
}

@Data
class InterventionRequest {
    private String type;
    private String description;
    private String status;
    private String equipe;
    private Long alerteId;
}
