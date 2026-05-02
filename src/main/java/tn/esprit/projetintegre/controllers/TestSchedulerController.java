package tn.esprit.projetintegre.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projetintegre.scheduler.SponsorTierUpgradeScheduler;

@RestController
@RequestMapping("/test")
public class TestSchedulerController {

    @Autowired
    private SponsorTierUpgradeScheduler scheduler;

    @PostMapping("/run-upgrade")
    public String runUpgrade() {
        scheduler.upgradeSponsorTiers();
        return "Scheduler executed. Check console logs.";
    }
}