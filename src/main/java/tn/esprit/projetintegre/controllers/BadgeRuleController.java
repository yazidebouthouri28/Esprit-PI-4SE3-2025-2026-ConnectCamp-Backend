package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.BadgeRule;
import tn.esprit.projetintegre.services.BadgeRuleService;

import java.util.List;

@RestController
@RequestMapping("/api/badge-rules")
@RequiredArgsConstructor
public class BadgeRuleController {

    private final BadgeRuleService badgeRuleService;

    @GetMapping
    public List<BadgeRule> getAll() {
        return badgeRuleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeRule> getById(@PathVariable Long id) {
        return badgeRuleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/badge/{badgeId}")
    public List<BadgeRule> getByBadgeId(@PathVariable Long badgeId) {
        return badgeRuleService.getByBadgeId(badgeId);
    }

    @PostMapping
    public BadgeRule create(@RequestBody BadgeRule badgeRule) {
        return badgeRuleService.create(badgeRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BadgeRule> update(@PathVariable Long id, @RequestBody BadgeRule ruleDetails) {
        try {
            return ResponseEntity.ok(badgeRuleService.update(id, ruleDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        badgeRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
