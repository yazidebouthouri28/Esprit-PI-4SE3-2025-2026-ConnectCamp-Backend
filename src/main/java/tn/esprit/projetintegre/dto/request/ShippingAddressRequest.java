package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {
    private String label;

    @NotBlank(message = "Le nom du destinataire est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String recipientName;

    private String phone;

    @NotBlank(message = "L'adresse ligne 1 est obligatoire")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    private String state;

    @NotBlank(message = "Le code postal est obligatoire")
    private String postalCode;

    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    private Double latitude;
    private Double longitude;
    private String deliveryInstructions;
    private Boolean isDefault = false;

    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;

    public String getLabel() {
        return label;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public Long getUserId() {
        return userId;
    }
}
