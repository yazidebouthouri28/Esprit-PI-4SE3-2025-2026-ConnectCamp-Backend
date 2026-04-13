package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.UserMedal;
import tn.esprit.projetintegre.services.UserMedalService;

import java.util.List;

@RestController
@RequestMapping("/api/user-medals")
@RequiredArgsConstructor
public class UserMedalController {

    private final UserMedalService userMedalService;

    @GetMapping
    public List<UserMedal> getAll() {
        return userMedalService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<UserMedal> getByUserId(@PathVariable Long userId) {
        return userMedalService.getByUserId(userId);
    }

    @PostMapping
    public UserMedal create(@RequestBody UserMedal userMedal) {
        return userMedalService.create(userMedal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userMedalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
