package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.UserBadge;
import tn.esprit.projetintegre.services.UserBadgeService;

import java.util.List;

@RestController
@RequestMapping("/api/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    @GetMapping
    public List<UserBadge> getAll() {
        return userBadgeService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<UserBadge> getByUserId(@PathVariable Long userId) {
        return userBadgeService.getByUserId(userId);
    }

    @PostMapping
    public UserBadge create(@RequestBody UserBadge userBadge) {
        return userBadgeService.create(userBadge);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userBadgeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
