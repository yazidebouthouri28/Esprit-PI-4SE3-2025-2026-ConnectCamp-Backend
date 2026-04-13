package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Medal;
import tn.esprit.projetintegre.repositories.MedalRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedalService {

    private final MedalRepository medalRepository;

    public List<Medal> getAll() {
        return medalRepository.findAll();
    }

    public Optional<Medal> getById(Long id) {
        return medalRepository.findById(id);
    }

    @Transactional
    public Medal create(Medal medal) {
        return medalRepository.save(medal);
    }

    @Transactional
    public Medal update(Long id, Medal medalDetails) {
        Medal medal = medalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medal not found with id: " + id));
        medal.setName(medalDetails.getName());
        medal.setIcon(medalDetails.getIcon());
        return medalRepository.save(medal);
    }

    @Transactional
    public void delete(Long id) {
        medalRepository.deleteById(id);
    }
}
