package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.EmergencyAlert;
import tn.esprit.projetintegre.entities.EmergencyIntervention;
import tn.esprit.projetintegre.enums.EmergencyType;
import tn.esprit.projetintegre.repositories.EmergencyAlertRepository;
import tn.esprit.projetintegre.repositories.EmergencyInterventionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmergencyInterventionService {

    private final EmergencyInterventionRepository interventionRepository;
    private final EmergencyAlertRepository alertRepository;

    public EmergencyIntervention createIntervention(Long alerteId, String type, String description, String status, String equipe) {
        EmergencyAlert alert = alertRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte introuvable avec l'ID: " + alerteId));
        
        EmergencyIntervention intervention = EmergencyIntervention.builder()
                .interventionCode("INT-" + System.currentTimeMillis())
                .title("Intervention sur l'alerte " + alerteId)
                .description(description)
                .interventionType(type != null ? EmergencyType.valueOf(type) : EmergencyType.OTHER)
                .status(status != null ? status : "ON_SITE")
                .deployedResources(equipe)
                .alert(alert)
                .dispatchedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
        return interventionRepository.save(intervention);
    }

    public List<EmergencyIntervention> getInterventionsByAlert(Long alertId) {
        return interventionRepository.findByAlertId(alertId);
    }
}
