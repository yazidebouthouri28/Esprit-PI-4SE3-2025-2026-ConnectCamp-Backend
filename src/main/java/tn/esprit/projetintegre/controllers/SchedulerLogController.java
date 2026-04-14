package tn.esprit.projetintegre.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.SchedulerLog;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;

@RestController
@RequestMapping("/api/scheduler-logs")
@CrossOrigin(origins = "*") // Needed for Angular
public class SchedulerLogController {

    @Autowired
    private SchedulerLogRepository schedulerLogRepository;

    @GetMapping
    public ResponseEntity<Page<SchedulerLog>> getLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        Page<SchedulerLog> logs = schedulerLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "executedAt")));
        
        return ResponseEntity.ok(logs);
    }
}
