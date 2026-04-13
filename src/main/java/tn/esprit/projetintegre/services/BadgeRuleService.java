package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.BadgeRule;
import tn.esprit.projetintegre.repositories.BadgeRuleRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BadgeRuleService {

    private final BadgeRuleRepository badgeRuleRepository;

    public List<BadgeRule> getAll() {
        return badgeRuleRepository.findAll();
    }

    public List<BadgeRule> getByBadgeId(Long badgeId) {
        return badgeRuleRepository.findByBadgeIdOrderByNumeroAsc(badgeId);
    }

    public Optional<BadgeRule> getById(Long id) {
        return badgeRuleRepository.findById(id);
    }

    @Transactional
    public BadgeRule create(BadgeRule badgeRule) {
        return badgeRuleRepository.save(badgeRule);
    }

    @Transactional
    public BadgeRule update(Long id, BadgeRule ruleDetails) {
        BadgeRule rule = badgeRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BadgeRule not found with id: " + id));
        rule.setNumero(ruleDetails.getNumero());
        rule.setRegle(ruleDetails.getRegle());
        return badgeRuleRepository.save(rule);
    }

    @Transactional
    public void delete(Long id) {
        badgeRuleRepository.deleteById(id);
    }
}
