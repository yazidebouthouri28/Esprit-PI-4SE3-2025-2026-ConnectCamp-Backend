package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.nadineentities.Wallet;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Wallet> findByUserId(Long userId);
}