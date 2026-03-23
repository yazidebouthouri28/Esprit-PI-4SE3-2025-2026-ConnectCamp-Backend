package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressResponse {
    private Long id;
    private String label;
    private String recipientName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private String deliveryInstructions;
    private Boolean isDefault;
    private Boolean isActive;
    private Long userId;
    private String fullAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

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

    public Boolean getIsActive() {
        return isActive;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static ShippingAddressResponseBuilder builder() {
        return new ShippingAddressResponseBuilder();
    }

    public static class ShippingAddressResponseBuilder {
        private ShippingAddressResponse response = new ShippingAddressResponse();

        public ShippingAddressResponseBuilder id(Long id) {
            response.id = id;
            return this;
        }

        public ShippingAddressResponseBuilder label(String label) {
            response.label = label;
            return this;
        }

        public ShippingAddressResponseBuilder recipientName(String recipientName) {
            response.recipientName = recipientName;
            return this;
        }

        public ShippingAddressResponseBuilder phone(String phone) {
            response.phone = phone;
            return this;
        }

        public ShippingAddressResponseBuilder addressLine1(String addressLine1) {
            response.addressLine1 = addressLine1;
            return this;
        }

        public ShippingAddressResponseBuilder addressLine2(String addressLine2) {
            response.addressLine2 = addressLine2;
            return this;
        }

        public ShippingAddressResponseBuilder city(String city) {
            response.city = city;
            return this;
        }

        public ShippingAddressResponseBuilder state(String state) {
            response.state = state;
            return this;
        }

        public ShippingAddressResponseBuilder postalCode(String postalCode) {
            response.postalCode = postalCode;
            return this;
        }

        public ShippingAddressResponseBuilder country(String country) {
            response.country = country;
            return this;
        }

        public ShippingAddressResponseBuilder latitude(Double latitude) {
            response.latitude = latitude;
            return this;
        }

        public ShippingAddressResponseBuilder longitude(Double longitude) {
            response.longitude = longitude;
            return this;
        }

        public ShippingAddressResponseBuilder deliveryInstructions(String deliveryInstructions) {
            response.deliveryInstructions = deliveryInstructions;
            return this;
        }

        public ShippingAddressResponseBuilder isDefault(Boolean isDefault) {
            response.isDefault = isDefault;
            return this;
        }

        public ShippingAddressResponseBuilder isActive(Boolean isActive) {
            response.isActive = isActive;
            return this;
        }

        public ShippingAddressResponseBuilder userId(Long userId) {
            response.userId = userId;
            return this;
        }

        public ShippingAddressResponseBuilder fullAddress(String fullAddress) {
            response.fullAddress = fullAddress;
            return this;
        }

        public ShippingAddressResponseBuilder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public ShippingAddressResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public ShippingAddressResponse build() {
            return response;
        }
    }
}
