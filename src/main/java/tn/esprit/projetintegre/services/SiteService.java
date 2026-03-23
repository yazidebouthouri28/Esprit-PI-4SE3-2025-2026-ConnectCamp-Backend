package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    public Page<Site> getActiveSites(Pageable pageable) {
        return siteRepository.findByIsActiveTrue(pageable);
    }

    public Site getSiteById(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
    }

    public Page<Site> searchSites(String keyword, Pageable pageable) {
        return siteRepository.searchSites(keyword, pageable);
    }

    public Page<Site> getSitesByOwner(Long ownerId, Pageable pageable) {
        return siteRepository.findByOwnerId(ownerId, pageable);
    }

    public List<Site> getSitesByCity(String city) {
        return siteRepository.findByCity(city);
    }

    public List<Site> getSitesByType(String type) {
        return siteRepository.findByType(type);
    }

    @Transactional
    public Site createSite(Site site, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        site.setOwner(owner);
        return siteRepository.save(site);
    }

    @Transactional
    public Site updateSite(Long id, Site siteDetails) {
        Site site = getSiteById(id);

        if (siteDetails.getName() != null) site.setName(siteDetails.getName());
        if (siteDetails.getDescription() != null) site.setDescription(siteDetails.getDescription());
        if (siteDetails.getType() != null) site.setType(siteDetails.getType());
        if (siteDetails.getAddress() != null) site.setAddress(siteDetails.getAddress());
        if (siteDetails.getCity() != null) site.setCity(siteDetails.getCity());
        if (siteDetails.getCountry() != null) site.setCountry(siteDetails.getCountry());
        if (siteDetails.getCapacity() != null) site.setCapacity(siteDetails.getCapacity());
        if (siteDetails.getPricePerNight() != null) site.setPricePerNight(siteDetails.getPricePerNight());
        if (siteDetails.getImages() != null) site.setImages(siteDetails.getImages());
        if (siteDetails.getAmenities() != null) site.setAmenities(siteDetails.getAmenities());
        if (siteDetails.getContactPhone() != null) site.setContactPhone(siteDetails.getContactPhone());
        if (siteDetails.getContactEmail() != null) site.setContactEmail(siteDetails.getContactEmail());

        return siteRepository.save(site);
    }

    @Transactional
    public void deleteSite(Long id) {
        Site site = getSiteById(id);
        site.setIsActive(false);
        siteRepository.save(site);
    }
}
