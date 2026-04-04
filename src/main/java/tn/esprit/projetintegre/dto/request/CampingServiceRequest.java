package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.ServiceType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampingServiceRequest {
    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Service type is required")
    private ServiceType type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    private String pricingUnit;
    private List<String> images;
    private Boolean isActive;
    private Boolean isAvailable;

    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    private Long providerId;
    private Long siteId;

    private Boolean isCamperOnly;
    private Boolean isOrganizerService;
}
