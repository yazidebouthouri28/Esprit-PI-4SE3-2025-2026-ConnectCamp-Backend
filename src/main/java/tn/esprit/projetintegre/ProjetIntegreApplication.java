package tn.esprit.projetintegre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProjetIntegreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetIntegreApplication.class, args);
    }
}
