package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projetintegre.entities.Reservation;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.repositories.ReservationRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlIntegrationService {

    private final SiteRepository siteRepository;
    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ml.api.url:http://localhost:8000}")
    private String mlApiUrl;

    private Object convertKeysToCamelCase(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> camelMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                camelMap.put(toCamelCase(entry.getKey()), convertKeysToCamelCase(entry.getValue()));
            }
            return camelMap;
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> camelList = new ArrayList<>();
            for (Object item : list) {
                camelList.add(convertKeysToCamelCase(item));
            }
            return camelList;
        }
        return obj;
    }

    private String toCamelCase(String snakeCase) {
        Matcher matcher = Pattern.compile("_[a-zA-Z]").matcher(snakeCase);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().substring(1).toUpperCase());
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public Object analyzeImage(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(mlApiUrl + "/ml/image/analyze", requestEntity, Map.class);
            return convertKeysToCamelCase(response.getBody());
        } catch (IOException e) {
            log.error("Failed to read image bytes", e);
            throw new RuntimeException("Failed to analyze image", e);
        }
    }

    public Object getOptimalPrice(Long siteId, Double currentPrice) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Site not found with ID: " + siteId));

        Map<String, Object> request = new HashMap<>();
        request.put("site_id", site.getId());
        request.put("current_price", currentPrice != null ? currentPrice : (site.getPricePerNight() != null ? site.getPricePerNight().doubleValue() : 50.0));
        request.put("capacity", site.getCapacity() != null ? site.getCapacity() : 2);
        request.put("rating", site.getAverageRating() != null ? site.getAverageRating().doubleValue() : 4.0);
        request.put("review_count", site.getReviewCount() != null ? site.getReviewCount() : 0);
        request.put("month", LocalDateTime.now().getMonthValue());
        request.put("amenities_count", site.getAmenities() != null ? site.getAmenities().size() : 0);

        ResponseEntity<Map> response = restTemplate.postForEntity(mlApiUrl + "/ml/pricing/optimize", request, Map.class);
        return convertKeysToCamelCase(response.getBody());
    }

    public Object analyzeCancellationRisk(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + reservationId));

        Map<String, Object> request = new HashMap<>();
        
        long leadTime = 0;
        if (reservation.getCreatedAt() != null && reservation.getCheckInDate() != null) {
            leadTime = java.time.temporal.ChronoUnit.DAYS.between(reservation.getCreatedAt(), reservation.getCheckInDate());
        }
        
        request.put("lead_time", Math.max(0, leadTime));
        request.put("arrival_month", reservation.getCheckInDate() != null ? reservation.getCheckInDate().getMonthValue() : LocalDateTime.now().getMonthValue());
        request.put("weekend_nights", 0); // Simplified
        request.put("week_nights", reservation.getNumberOfNights() != null ? reservation.getNumberOfNights() : 1);
        request.put("adults", reservation.getNumberOfGuests() != null ? reservation.getNumberOfGuests() : 2);
        request.put("children", 0);
        request.put("babies", 0);
        request.put("is_repeated_guest", 0);
        request.put("previous_cancellations", 0);
        request.put("previous_bookings_not_canceled", 0);
        request.put("booking_changes", 0);
        request.put("deposit_type", "No Deposit");
        request.put("special_requests_count", reservation.getSpecialRequests() != null && !reservation.getSpecialRequests().isEmpty() ? 1 : 0);
        request.put("required_car_parking_spaces", 0);
        
        Site site = reservation.getSite();
        request.put("site_rating", site != null && site.getAverageRating() != null ? site.getAverageRating().doubleValue() : 4.0);
        request.put("adr", reservation.getPricePerNight() != null ? reservation.getPricePerNight().doubleValue() : 100.0);

        ResponseEntity<Map> response = restTemplate.postForEntity(mlApiUrl + "/ml/reservation/cancellation-risk", request, Map.class);
        return convertKeysToCamelCase(response.getBody());
    }

    public Object classifyHighlight(Map<String, String> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(mlApiUrl + "/ml/highlight/classify", request, Map.class);
            return convertKeysToCamelCase(response.getBody());
        } catch (Exception e) {
            log.error("Failed to classify highlight", e);
            throw new RuntimeException("Failed to classify highlight", e);
        }
    }
}
