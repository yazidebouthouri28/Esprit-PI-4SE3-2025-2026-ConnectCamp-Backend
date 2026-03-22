package tn.esprit.projetintegre.controllernadine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.servicenadine.PointsService;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/{userId}/balance")
    public ResponseEntity<Integer> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getAvailablePoints(userId));
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirePoints() {
        pointsService.expirePoints();
        return ResponseEntity.ok().build();
    }
}
