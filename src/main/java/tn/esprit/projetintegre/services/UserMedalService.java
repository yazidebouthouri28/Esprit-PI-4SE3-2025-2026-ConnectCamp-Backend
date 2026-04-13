package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.UserMedal;
import tn.esprit.projetintegre.repositories.UserMedalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMedalService {

    private final UserMedalRepository userMedalRepository;

    public List<UserMedal> getAll() {
        return userMedalRepository.findAll();
    }

    public List<UserMedal> getByUserId(Long userId) {
        return userMedalRepository.findByUserId(userId);
    }

    @Transactional
    public UserMedal create(UserMedal userMedal) {
        return userMedalRepository.save(userMedal);
    }

    @Transactional
    public void delete(Long id) {
        userMedalRepository.deleteById(id);
    }
}
