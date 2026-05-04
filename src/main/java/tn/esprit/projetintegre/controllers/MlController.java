package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projetintegre.services.MlIntegrationService;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MlController {

    private final MlIntegrationService mlIntegrationService;

    @PostMapping(value = "/image/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> analyzeImage(@RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(mlIntegrationService.analyzeImage(image));
    }

    @PostMapping("/campsite/{siteId}/optimal-price")
    public ResponseEntity<Object> getOptimalPrice(
            @PathVariable Long siteId,
            @RequestParam(required = false) Double currentPrice) {
        return ResponseEntity.ok(mlIntegrationService.getOptimalPrice(siteId, currentPrice));
    }

    @PostMapping("/reservation/{reservationId}/cancellation-risk")
    public ResponseEntity<Object> analyzeCancellationRisk(@PathVariable Long reservationId) {
        return ResponseEntity.ok(mlIntegrationService.analyzeCancellationRisk(reservationId));
    }

    @PostMapping("/highlight/classify")
    public ResponseEntity<Object> classifyHighlight(@RequestBody java.util.Map<String, String> request) {
        return ResponseEntity.ok(mlIntegrationService.classifyHighlight(request));
    }
}
