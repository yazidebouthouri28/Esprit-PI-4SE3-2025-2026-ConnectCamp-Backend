package tn.esprit.projetintegre.services;

import tn.esprit.projetintegre.dto.request.MLPredictionRequest;
import tn.esprit.projetintegre.dto.response.MLPredictionResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import tn.esprit.projetintegre.entities.Event;

@Service
public class EventMLService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ML_API_URL = "http://localhost:5000/predict";

    /**
     * Appelle l'API Flask ML pour prédire la popularité d'un événement
     * 
     * @param request Catégorie, état, heure, mois, jour
     * @return Prédiction (participants, popularité, badge)
     */
    public MLPredictionResponse predictPopularity(MLPredictionRequest request) {

        // 1. Préparer les headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Créer la requête HTTP
        HttpEntity<MLPredictionRequest> httpRequest = new HttpEntity<>(request, headers);

        try {
            // 3. Envoyer la requête POST à l'API Flask
            ResponseEntity<MLPredictionResponse> response = restTemplate.postForEntity(
                    ML_API_URL,
                    httpRequest,
                    MLPredictionResponse.class);

            // 4. Retourner la réponse
            return response.getBody();

        } catch (RestClientException e) {
            // 5. Gestion d'erreur si l'API ML n'est pas disponible
            System.err.println("❌ Erreur appel API ML: " + e.getMessage());

            // Retourner une réponse par défaut (fallback)
            MLPredictionResponse fallback = new MLPredictionResponse();
            fallback.setPredicted_attendees(0);
            fallback.setPopularity("unknown");
            fallback.setBadge_suggestion("API offline");
            return heuristicFallback(request);
        }
    }

    /**
     * Vérifie si l'API ML est accessible
     */
    public boolean isMLApiAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:5000/health",
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private MLPredictionResponse heuristicFallback(MLPredictionRequest request) {
        final String category = String.valueOf(request.getCategory() == null ? "" : request.getCategory()).trim()
                .toLowerCase();
        final String state = String.valueOf(request.getState() == null ? "" : request.getState()).trim().toLowerCase();
        final int hour = request.getHour();
        final int month = request.getMonth();
        final int dow = request.getDay_of_week();

        int base;
        if (category.contains("music") || category.contains("festival"))
            base = 55;
        else if (category.contains("sport"))
            base = 45;
        else if (category.contains("tech"))
            base = 35;
        else if (category.contains("education") || category.contains("workshop"))
            base = 25;
        else if (category.contains("culture") || category.contains("exhibition"))
            base = 30;
        else if (category.contains("adventure") || category.contains("nature"))
            base = 28;
        else
            base = 20;

        int timeBoost = (hour >= 18 && hour <= 22) ? 12 : (hour >= 10 && hour <= 16) ? 6 : 0;
        int summerBoost = (month >= 6 && month <= 9) ? 8 : 0;
        int weekendBoost = (dow == 5 || dow == 6) ? 10 : 0;
        int cityBoost = (state.contains("tunis") || state.contains("sf") || state.contains("san")) ? 6 : 0;

        int predicted = Math.max(0, base + timeBoost + summerBoost + weekendBoost + cityBoost);

        String popularity;
        if (predicted < 5)
            popularity = "low";
        else if (predicted < 20)
            popularity = "medium";
        else if (predicted < 50)
            popularity = "high";
        else
            popularity = "very_high";

        String badge;
        switch (popularity) {
            case "medium":
                badge = "Connector";
                break;
            case "high":
                badge = "Networker";
                break;
            case "very_high":
                badge = "Community Leader";
                break;
            default:
                badge = "Explorer";
        }

        MLPredictionResponse fallback = new MLPredictionResponse();
        fallback.setPredicted_attendees(predicted);
        fallback.setPopularity(popularity);
        fallback.setBadge_suggestion(badge);
        return fallback;
    }

    public void sendRetrainingData(Event event) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Calculer duration_hours
        long durationHours = 2;
        if (event.getStartDate() != null && event.getEndDate() != null) {
            durationHours = java.time.Duration.between(
                    event.getStartDate(), event.getEndDate()).toHours();
            durationHours = Math.max(1, durationHours);
        }

        // Construire le payload
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("category", event.getCategory() != null ? event.getCategory() : "Other");
        payload.put("event_type", event.getEventType() != null ? event.getEventType() : "Other");
        payload.put("state", event.getLocation() != null ? event.getLocation() : "Unknown");
        payload.put("hour", event.getStartDate() != null ? event.getStartDate().getHour() : 12);
        payload.put("month", event.getStartDate() != null ? event.getStartDate().getMonthValue() : 6);
        payload.put("day_of_week",
                event.getStartDate() != null ? event.getStartDate().getDayOfWeek().getValue() - 1 : 2);
        payload.put("duration_hours", durationHours);
        payload.put("price", event.getPrice() != null ? event.getPrice().doubleValue() : 0.0);
        payload.put("actual_attendees", event.getActualAttendees() != null ? event.getActualAttendees() : 0);

        HttpEntity<java.util.Map<String, Object>> httpRequest = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity("http://localhost:5000/retrain", httpRequest, String.class);
            System.out.println("✅ Données envoyées pour réentraînement: " + event.getTitle());
        } catch (RestClientException e) {
            System.err.println("⚠️ Réentraînement échoué (non bloquant): " + e.getMessage());
        }
    }
}
