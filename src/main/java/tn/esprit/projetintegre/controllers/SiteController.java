package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.request.SiteRequest;
import tn.esprit.projetintegre.dto.response.SiteResponse;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.SiteService;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@Tag(name = "Sites", description = "Site/Location management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class SiteController {

    private final SiteService siteService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all active sites")
    public ResponseEntity<ApiResponse<PageResponse<SiteResponse>>> getAllSites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Site> sites = siteService.getActiveSites(PageRequest.of(page, size));
        Page<SiteResponse> response = sites.map(dtoMapper::toSiteResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get site by ID")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(@PathVariable Long id) {
        Site site = siteService.getSiteById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toSiteResponse(site)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search sites")
    public ResponseEntity<ApiResponse<PageResponse<SiteResponse>>> searchSites(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Site> sites = siteService.searchSites(keyword, PageRequest.of(page, size));
        Page<SiteResponse> response = sites.map(dtoMapper::toSiteResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get sites by city")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSitesByCity(@PathVariable String city) {
        List<Site> sites = siteService.getSitesByCity(city);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toSiteResponseList(sites)));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get sites by type")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSitesByType(@PathVariable String type) {
        List<Site> sites = siteService.getSitesByType(type);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toSiteResponseList(sites)));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get sites by owner")
    public ResponseEntity<ApiResponse<PageResponse<SiteResponse>>> getSitesByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Site> sites = siteService.getSitesByOwner(ownerId, PageRequest.of(page, size));
        Page<SiteResponse> response = sites.map(dtoMapper::toSiteResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Create a site")
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @Valid @RequestBody SiteRequest request,
            @RequestParam Long ownerId) {
        Site site = toEntity(request);
        Site created = siteService.createSite(site, ownerId);
        return ResponseEntity.ok(ApiResponse.success("Site created successfully", dtoMapper.toSiteResponse(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update a site")
    public ResponseEntity<ApiResponse<SiteResponse>> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {
        Site siteDetails = toEntity(request);
        Site updated = siteService.updateSite(id, siteDetails);
        return ResponseEntity.ok(ApiResponse.success("Site updated successfully", dtoMapper.toSiteResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Delete a site")
    public ResponseEntity<ApiResponse<Void>> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.ok(ApiResponse.success("Site deleted", null));
    }

    private Site toEntity(SiteRequest request) {
        return Site.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacity(request.getCapacity())
                .pricePerNight(request.getPricePerNight())
                .images(request.getImages())
                .amenities(request.getAmenities())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .isActive(request.getIsActive())
                .build();
    }
}
