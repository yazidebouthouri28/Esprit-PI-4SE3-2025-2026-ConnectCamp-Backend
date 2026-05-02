package tn.esprit.projetintegre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import tn.esprit.projetintegre.entities.SchedulerLog;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ProjetIntegreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetIntegreApplication.class, args);
    }

    @Bean
    public CommandLineRunner initLogs(SchedulerLogRepository repo) {
        return args -> {
            repo.save(SchedulerLog.builder()
                    .schedulerName("SystemManager")
                    .details("Backend started - Monitoring active.")
                    .build());
            System.out.println("[SYSTEM] Initial log entry created successfully.");
        };
    }
}
