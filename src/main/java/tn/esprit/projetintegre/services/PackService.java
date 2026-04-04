package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.PackDTO;
import tn.esprit.projetintegre.entities.Pack;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.enums.PackType;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.repositories.PackRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.CampingServiceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PackService {

    private final PackRepository packRepository;
    private final SiteRepository siteRepository;
    private final CampingServiceRepository campingServiceRepository;

    private void calculateAndValidatePricing(Pack pack) {

        if (pack.getValidFrom() != null && pack.getValidUntil() != null
                && pack.getValidFrom().isAfter(pack.getValidUntil())) {
            throw new BusinessException("La date de début doit être antérieure à la date de fin de validité.");
        }

        if (pack.getServices() != null && !pack.getServices().isEmpty()) {
            BigDecimal computedOriginalPrice = BigDecimal.ZERO;
            for (CampingService service : pack.getServices()) {
                if (service.getPrice() != null) {
                    computedOriginalPrice = computedOriginalPrice.add(service.getPrice());
                }
            }
            pack.setOriginalPrice(computedOriginalPrice);
        } else {
            pack.setOriginalPrice(pack.getPrice());
        }

        if (pack.getOriginalPrice() != null && pack.getPrice() != null) {
            if (pack.getPrice().compareTo(pack.getOriginalPrice()) >= 0 && pack.getServices() != null
                    && !pack.getServices().isEmpty()) {
                throw new BusinessException("Le prix du pack (" + pack.getPrice()
                        + ") doit être inférieur au prix normal cumulé (" + pack.getOriginalPrice() + ").");
            }

            if (pack.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal economy = pack.getOriginalPrice().subtract(pack.getPrice());
                BigDecimal percentage = economy.divide(pack.getOriginalPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                pack.setDiscountPercentage(percentage.doubleValue());
            } else {
                pack.setDiscountPercentage(0.0);
            }
        }
    }

    public PackDTO.Response createPack(PackDTO.CreateRequest request) {
        // Only ADMIN can create packs
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can create packs");
        }
        Pack pack = Pack.builder()
                .name(request.getName())
                .description(request.getDescription())
                .packType(request.getPackType())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxPersons(request.getMaxPersons())
                .imageUrl(request.getImageUrl()) // Support URL
                .image(request.getImage() != null ? request.getImage()
                        : (request.getImages() != null && !request.getImages().isEmpty() ? request.getImages().get(0)
                                : null))
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .inclusions(request.getInclusions() != null ? request.getInclusions() : new ArrayList<>())
                .exclusions(request.getExclusions() != null ? request.getExclusions() : new ArrayList<>())
                .isActive(true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isLimitedOffer(request.getIsLimitedOffer() != null ? request.getIsLimitedOffer() : false)
                .availableQuantity(request.getAvailableQuantity())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .services(new ArrayList<>())
                .build();

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Site non trouvé avec l'ID: " + request.getSiteId()));
            pack.setSite(site);
        }

        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<CampingService> services = campingServiceRepository.findAllById(request.getServiceIds());
            pack.setServices(services);
        }

        calculateAndValidatePricing(pack);

        pack = packRepository.save(pack);
        return toResponse(pack);
    }

    @Transactional(readOnly = true)
    public PackDTO.Response getById(Long id) {
        tn.esprit.projetintegre.repositories.projections.PackProjection projection = packRepository.findByIdProjected(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id));
        return toResponse(projection);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getAllActive(Pageable pageable) {
        return packRepository.findByIsActiveTrueProjected(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getAllAdmin(Pageable pageable) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can view all packs");
        }
        return packRepository.findAllProjected(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getByType(PackType type, Pageable pageable) {
        return packRepository.findByPackTypeProjected(type, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getBySiteId(Long siteId, Pageable pageable) {
        return packRepository.findBySiteIdProjected(siteId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PackDTO.Response> getFeaturedPacks() {
        return packRepository.findFeaturedPacks().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> searchPacks(String keyword, Pageable pageable) {
        return packRepository.searchPacksProjected(keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return packRepository.findByPriceRange(minPrice, maxPrice, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PackDTO.Response> getTopSellingPacks(int limit) {
        return packRepository.findTopSellingPacks(PageRequest.of(0, limit)).stream().map(this::toResponse).toList();
    }

    public PackDTO.Response updatePack(Long id, PackDTO.UpdateRequest request) {
        // Only ADMIN can update packs
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can update packs");
        }
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id));

        if (request.getName() != null)
            pack.setName(request.getName());
        if (request.getDescription() != null)
            pack.setDescription(request.getDescription());
        if (request.getPackType() != null)
            pack.setPackType(request.getPackType());
        if (request.getPrice() != null)
            pack.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null)
            pack.setOriginalPrice(request.getOriginalPrice());
        if (request.getDurationDays() != null)
            pack.setDurationDays(request.getDurationDays());
        if (request.getMaxPersons() != null)
            pack.setMaxPersons(request.getMaxPersons());
        if (request.getImageUrl() != null)
            pack.setImageUrl(request.getImageUrl()); // Support URL
        if (request.getImage() != null)
            pack.setImage(request.getImage());
        if (request.getImages() != null) {
            pack.setImages(request.getImages());
            if (pack.getImage() == null && !request.getImages().isEmpty()) {
                pack.setImage(request.getImages().get(0));
            }
        }
        if (request.getFeatures() != null)
            pack.setFeatures(request.getFeatures());
        if (request.getInclusions() != null)
            pack.setInclusions(request.getInclusions());
        if (request.getExclusions() != null)
            pack.setExclusions(request.getExclusions());
        if (request.getIsActive() != null)
            pack.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null)
            pack.setIsFeatured(request.getIsFeatured());
        if (request.getIsLimitedOffer() != null)
            pack.setIsLimitedOffer(request.getIsLimitedOffer());
        if (request.getAvailableQuantity() != null)
            pack.setAvailableQuantity(request.getAvailableQuantity());
        if (request.getValidFrom() != null)
            pack.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null)
            pack.setValidUntil(request.getValidUntil());
        if (request.getServiceIds() != null) {
            List<CampingService> services = campingServiceRepository.findAllById(request.getServiceIds());
            pack.setServices(services);
        }

        calculateAndValidatePricing(pack);

        pack = packRepository.save(pack);
        return toResponse(pack);
    }

    public void deletePack(Long id) {
        // Only ADMIN can delete packs
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can delete packs");
        }
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id));
        pack.setIsActive(false);
        packRepository.save(pack);
    }

    private PackDTO.Response toResponse(tn.esprit.projetintegre.repositories.projections.PackProjection projection) {
        return PackDTO.Response.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .packType(projection.getPackType())
                .price(projection.getPrice())
                .originalPrice(projection.getOriginalPrice())
                .discountPercentage(calculateDiscount(projection.getPrice(), projection.getOriginalPrice()))
                .durationDays(projection.getDurationDays())
                .maxPersons(projection.getMaxPersons())
                .isActive(projection.getIsActive())
                .isFeatured(projection.getIsFeatured())
                .isLimitedOffer(projection.getIsLimitedOffer())
                .availableQuantity(projection.getAvailableQuantity())
                .soldCount(projection.getSoldCount())
                .rating(projection.getRating())
                .reviewCount(projection.getReviewCount())
                .validFrom(projection.getValidFrom())
                .validUntil(projection.getValidUntil())
                .siteId(projection.getSiteId())
                .siteName(projection.getSiteName())
                .imageUrl(projection.getImageUrl())
                .serviceCount(projection.getServiceCount())
                .createdAt(projection.getCreatedAt())
                .serviceIds(new java.util.ArrayList<>()) // Services non chargés dans la projection pour stabilité
                .serviceNames(new java.util.ArrayList<>())
                .build();
    }

    private PackDTO.Response toResponse(Pack pack) {
        return PackDTO.Response.builder()
                .id(pack.getId())
                .name(pack.getName())
                .description(pack.getDescription())
                .packType(pack.getPackType())
                .price(pack.getPrice())
                .originalPrice(pack.getOriginalPrice())
                .discountPercentage(pack.getDiscountPercentage())
                .serviceCount(pack.getServices() != null ? pack.getServices().size() : 0)
                .durationDays(pack.getDurationDays())
                .maxPersons(pack.getMaxPersons())
                // .image(pack.getImage()) // Reste désactivé car cause des 500/Link Failure
                // .images(pack.getImages()) // Reste désactivé car cause des 500/Link Failure
                .isActive(pack.getIsActive())
                .isFeatured(pack.getIsFeatured())
                .isLimitedOffer(pack.getIsLimitedOffer())
                .availableQuantity(pack.getAvailableQuantity())
                .soldCount(pack.getSoldCount())
                .rating(pack.getRating())
                .reviewCount(pack.getReviewCount())
                .validFrom(pack.getValidFrom())
                .validUntil(pack.getValidUntil())
                .siteId(pack.getSite() != null ? pack.getSite().getId() : null)
                .siteName(pack.getSite() != null ? pack.getSite().getName() : null)
                .imageUrl(pack.getImageUrl()) // Mappage de imageUrl
                .createdAt(pack.getCreatedAt())
                .serviceIds(pack.getServices() != null
                        ? pack.getServices().stream().map(CampingService::getId).toList()
                        : new java.util.ArrayList<>())
                .serviceNames(pack.getServices() != null
                        ? pack.getServices().stream().map(CampingService::getName).toList()
                        : new java.util.ArrayList<>())
                .build();
    }

    private Double calculateDiscount(BigDecimal price, BigDecimal originalPrice) {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            return originalPrice.subtract(price).divide(originalPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }
}
