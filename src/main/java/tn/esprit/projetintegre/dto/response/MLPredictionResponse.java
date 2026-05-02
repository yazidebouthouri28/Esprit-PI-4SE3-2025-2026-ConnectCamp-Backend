package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponse {
    private int predicted_attendees; // Nombre prédit de participants
    private String popularity; // "low", "medium", "high", "very_high"
    private String badge_suggestion; // "Explorer", "Connector", "Networker", "Community Leader"
}