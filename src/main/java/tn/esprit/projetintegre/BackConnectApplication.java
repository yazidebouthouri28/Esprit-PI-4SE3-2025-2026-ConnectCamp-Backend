package tn.esprit.projetintegre;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Compatibility entrypoint.
 * Keeps existing IDE run configurations working while ensuring
 * the same component scan/auditing behavior as {@link ProjetIntegreApplication}.
 */
@EnableScheduling
public class BackConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetIntegreApplication.class, args);
    }
}

