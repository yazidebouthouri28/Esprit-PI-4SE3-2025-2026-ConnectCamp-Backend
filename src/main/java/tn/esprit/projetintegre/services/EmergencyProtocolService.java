package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.entities.EmergencyProtocol;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EmergencyProtocolRepository;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyProtocolService {

    private final EmergencyProtocolRepository emergencyProtocolRepository;

    // Anyone authenticated can view active protocols
    public List<EmergencyProtocol> getAllActiveProtocols() {
        return emergencyProtocolRepository.findByIsActiveTrue(org.springframework.data.domain.Pageable.unpaged())
                .getContent();
    }

    public EmergencyProtocol getProtocolById(Long id) {
        return emergencyProtocolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyProtocol not found with id: " + id));
    }

    public EmergencyProtocol createProtocol(EmergencyProtocol protocol) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can create emergency protocols");
        }
        protocol.setIsActive(true);
        return emergencyProtocolRepository.save(protocol);
    }

    public EmergencyProtocol updateProtocol(Long id, EmergencyProtocol details) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can update emergency protocols");
        }
        EmergencyProtocol protocol = getProtocolById(id);
        // Update fields as needed (example: name, description, etc.)
        protocol.setName(details.getName());
        protocol.setDescription(details.getDescription());
        protocol.setEmergencyType(details.getEmergencyType());
        protocol.setStepsList(details.getStepsList());
        protocol.setRequiredEquipment(details.getRequiredEquipment());
        protocol.setEmergencyContacts(details.getEmergencyContacts());
        protocol.setAssemblyPoint(details.getAssemblyPoint());
        protocol.setEvacuationRoutes(details.getEvacuationRoutes());
        protocol.setEstimatedResponseTimeMinutes(details.getEstimatedResponseTimeMinutes());
        protocol.setPriorityLevel(details.getPriorityLevel());
        protocol.setRequiresTraining(details.getRequiresTraining());
        protocol.setNextReviewDate(details.getNextReviewDate());
        return emergencyProtocolRepository.save(protocol);
    }

    public void deleteProtocol(Long id) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can delete emergency protocols");
        }
        EmergencyProtocol protocol = getProtocolById(id);
        protocol.setIsActive(false);
        emergencyProtocolRepository.save(protocol);
    }
}
