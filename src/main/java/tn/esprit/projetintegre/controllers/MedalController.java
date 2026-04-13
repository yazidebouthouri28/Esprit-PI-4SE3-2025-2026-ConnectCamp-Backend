package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.Medal;
import tn.esprit.projetintegre.services.MedalService;

import java.util.List;

@RestController
@RequestMapping("/api/medals")
@RequiredArgsConstructor
public class MedalController {

    private final MedalService medalService;

    @GetMapping
    public List<Medal> getAll() {
        return medalService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medal> getById(@PathVariable Long id) {
        return medalService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Medal create(@RequestBody Medal medal) {
        return medalService.create(medal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medal> update(@PathVariable Long id, @RequestBody Medal medalDetails) {
        try {
            return ResponseEntity.ok(medalService.update(id, medalDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
