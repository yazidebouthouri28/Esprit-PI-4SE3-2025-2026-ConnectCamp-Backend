package tn.esprit.projetintegre.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.SponsorTier;
import tn.esprit.projetintegre.exception.DuplicateResourceException;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.repositories.SponsorshipRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SponsorServiceTest {

    @Mock
    private SponsorRepository sponsorRepository;
    @Mock
    private SponsorshipRepository sponsorshipRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SponsorService sponsorService;

    private Sponsor sponsor;

    @BeforeEach
    void setUp() {
        sponsor = Sponsor.builder()
                .id(1L)
                .name("Test Sponsor")
                .email("sponsor@test.com")
                .logo("logo.png")
                .tier(SponsorTier.GOLD)
                .isActive(false)
                .build();
    }

    @Test
    void createSponsor_shouldSetActiveTrue_andSave() {
        when(sponsorRepository.existsByEmail("sponsor@test.com")).thenReturn(false);
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sponsor created = sponsorService.createSponsor(sponsor);

        assertNotNull(created);
        assertTrue(created.getIsActive());
        verify(sponsorRepository).save(sponsor);
    }

    @Test
    void createSponsor_shouldThrow_whenEmailAlreadyExists() {
        when(sponsorRepository.existsByEmail("sponsor@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> sponsorService.createSponsor(sponsor));

        verify(sponsorRepository, never()).save(any(Sponsor.class));
    }

    @Test
    void updateSponsor_shouldUpdateFields_andSyncUserAvatar() {
        Sponsor existing = Sponsor.builder()
                .id(1L)
                .name("Old Name")
                .email("sponsor@test.com")
                .logo("old-logo.png")
                .tier(SponsorTier.BRONZE)
                .isActive(true)
                .build();

        Sponsor details = Sponsor.builder()
                .name("New Name")
                .description("New Desc")
                .logo("new-logo.png")
                .email("sponsor@test.com")
                .tier(SponsorTier.SILVER)
                .isActive(false)
                .build();

        User linkedUser = new User();
        linkedUser.setId(9L);
        linkedUser.setEmail("sponsor@test.com");
        linkedUser.setAvatar("old-avatar.png");

        when(sponsorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(linkedUser));

        Sponsor updated = sponsorService.updateSponsor(1L, details);

        assertEquals("New Name", updated.getName());
        assertEquals("new-logo.png", updated.getLogo());
        assertEquals(SponsorTier.SILVER, updated.getTier());
        assertFalse(updated.getIsActive());
        assertEquals("new-logo.png", linkedUser.getAvatar());

        verify(userRepository).save(linkedUser);
    }

    @Test
    void deleteSponsor_shouldSoftDelete() {
        Sponsor existing = Sponsor.builder().id(1L).isActive(true).build();
        when(sponsorRepository.findById(1L)).thenReturn(Optional.of(existing));

        sponsorService.deleteSponsor(1L);

        assertFalse(existing.getIsActive());
        verify(sponsorRepository).save(existing);
    }
}

