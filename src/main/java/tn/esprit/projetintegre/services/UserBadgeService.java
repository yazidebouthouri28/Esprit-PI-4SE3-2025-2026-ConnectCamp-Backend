package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.UserBadge;
import tn.esprit.projetintegre.entities.UserMedal;
import tn.esprit.projetintegre.repositories.BadgeRepository;
import tn.esprit.projetintegre.repositories.UserBadgeRepository;
import tn.esprit.projetintegre.repositories.UserMedalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final UserMedalRepository userMedalRepository;

    public List<UserBadge> getAll() {
        return userBadgeRepository.findAll();
    }

    public List<UserBadge> getByUserId(Long userId) {
        return userBadgeRepository.findByUserId(userId);
    }

    @Transactional
    public UserBadge create(UserBadge userBadge) {
        UserBadge savedBadge = userBadgeRepository.save(userBadge);
        updateMedalProgression(userBadge);
        return savedBadge;
    }

    private void updateMedalProgression(UserBadge userBadge) {
        if (userBadge.getBadge() == null || userBadge.getBadge().getMedal() == null)
            return;

        Long userId = userBadge.getUser().getId();
        Long medalId = userBadge.getBadge().getMedal().getId();

        boolean alreadyHasMedal = userMedalRepository.findByUserIdAndMedalId(userId, medalId).isPresent();

        if (!alreadyHasMedal) {
            UserMedal userMedal = new UserMedal();
            userMedal.setUser(userBadge.getUser());
            userMedal.setMedal(userBadge.getBadge().getMedal());
            userMedal.setEvent(userBadge.getEvent());
            userMedalRepository.save(userMedal);
        }
    }

    private void checkEagleOfCarthage(Long userId) {
        // logic removed as requested by level removal
    }

    @Transactional
    public void delete(Long id) {
        userBadgeRepository.deleteById(id);
    }
}
