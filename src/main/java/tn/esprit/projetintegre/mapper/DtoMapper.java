package tn.esprit.projetintegre.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.projetintegre.dto.response.*;
import tn.esprit.projetintegre.entities.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    // Category Mapping
    public CategoryResponse toCategoryResponse(Category entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .image(entity.getImage())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .displayOrder(entity.getDisplayOrder())
                .productCount(entity.getProducts() != null ? entity.getProducts().size() : 0)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<CategoryResponse> toCategoryResponseList(List<Category> entities) {
        return entities.stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    // Product Mapping
    public ProductResponse toProductResponse(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .discountPercentage(entity.getDiscountPercentage())
                .sku(entity.getSku())
                .barcode(entity.getBarcode())
                .brand(entity.getBrand())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .sellerId(entity.getSeller() != null ? entity.getSeller().getId() : null)
                .sellerName(entity.getSeller() != null ? entity.getSeller().getName() : null)
                .stockQuantity(entity.getStockQuantity())
                .minStockLevel(entity.getMinStockLevel())
                .images(entity.getImages())
                .thumbnail(entity.getThumbnail())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .salesCount(entity.getSalesCount())
                .viewCount(entity.getViewCount())
                .isActive(entity.getIsActive())
                .isFeatured(entity.getIsFeatured())
                .isOnSale(entity.getIsOnSale())
                .isRentable(entity.getIsRentable())
                .rentalPricePerDay(entity.getRentalPricePerDay())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toProductResponseList(List<Product> entities) {
        return entities.stream().map(this::toProductResponse).collect(Collectors.toList());
    }
    // Mission Mapping
    public MissionResponse toMissionResponse(Mission entity) {
        return MissionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .targetValue(entity.getTargetValue())
                .rewardPoints(entity.getRewardPoints())
                .rewardBadge(entity.getRewardBadge())
                .rewardDescription(entity.getRewardDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .isRepeatable(entity.getIsRepeatable())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<MissionResponse> toMissionResponseList(List<Mission> entities) {
        return entities.stream().map(this::toMissionResponse).collect(Collectors.toList());
    }

    // UserMission Mapping
    public UserMissionResponse toUserMissionResponse(UserMission entity) {
        return UserMissionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .missionId(entity.getMission() != null ? entity.getMission().getId() : null)
                .missionName(entity.getMission() != null ? entity.getMission().getName() : null)
                .currentProgress(entity.getCurrentProgress())
                .targetValue(entity.getMission() != null ? entity.getMission().getTargetValue() : 0)
                .isCompleted(entity.getIsCompleted())
                .rewardClaimed(entity.getRewardClaimed())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .rewardClaimedAt(entity.getRewardClaimedAt())
                .build();
    }
    // Notification Mapping
    public NotificationResponse toNotificationResponse(Notification entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .isRead(entity.getIsRead())
                .actionUrl(entity.getActionUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    }

    // Sponsorship Mapping
    public SponsorshipResponse toSponsorshipResponse(Sponsorship entity) {
        return SponsorshipResponse.builder()
                .id(entity.getId())
                .sponsorId(entity.getSponsor() != null ? entity.getSponsor().getId() : null)
                .sponsorName(entity.getSponsor() != null ? entity.getSponsor().getName() : null)
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .sponsorshipType(entity.getSponsorshipType())
                .sponsorshipLevel(entity.getSponsorshipLevel())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isPaid(entity.getIsPaid())
                .paidAt(entity.getPaidAt())
                .benefits(entity.getBenefits())
                .deliverables(entity.getDeliverables())
                .isActive(entity.getIsActive())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<SponsorshipResponse> toSponsorshipResponseList(List<Sponsorship> entities) {
        return entities.stream().map(this::toSponsorshipResponse).collect(Collectors.toList());
    }

    // Subscription Mapping
    public SubscriptionResponse toSubscriptionResponse(Subscription entity) {
        return SubscriptionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .planName(entity.getPlanName())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .autoRenew(entity.getAutoRenew())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<SubscriptionResponse> toSubscriptionResponseList(List<Subscription> entities) {
        return entities.stream().map(this::toSubscriptionResponse).collect(Collectors.toList());
    }
    // Transaction Mapping
    public TransactionResponse toTransactionResponse(Transaction entity) {
        return TransactionResponse.builder()
                .id(entity.getId())
                .transactionNumber(entity.getTransactionNumber())
                .amount(entity.getAmount())
                .type(entity.getType())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .walletId(entity.getWallet() != null ? entity.getWallet().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<TransactionResponse> toTransactionResponseList(List<Transaction> entities) {
        return entities.stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    // Wishlist Mapping
    public WishlistResponse toWishlistResponse(Wishlist entity) {
        return WishlistResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .name(entity.getName())
                .isPublic(entity.getIsPublic())
                .productCount(entity.getProducts() != null ? entity.getProducts().size() : 0)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<WishlistResponse> toWishlistResponseList(List<Wishlist> entities) {
        return entities.stream().map(this::toWishlistResponse).collect(Collectors.toList());
    }

    // Coupon Mapping
    public CouponResponse toCouponResponse(Coupon entity) {
        return CouponResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .discountType(entity.getType() != null ? entity.getType().name() : null)
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .usageLimitPerUser(entity.getUsageLimitPerUser())
                .isActive(entity.getIsActive())
                .isValid(entity.isValid())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<CouponResponse> toCouponResponseList(List<Coupon> entities) {
        return entities.stream().map(this::toCouponResponse).collect(Collectors.toList());
    }

    public EventResponse toEventResponse(Event entity) {
        return EventResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .eventType(entity.getEventType())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .maxParticipants(entity.getMaxParticipants())
                .currentParticipants(entity.getCurrentParticipants())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .images(entity.getImages())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .organizerId(entity.getOrganizer() != null ? entity.getOrganizer().getId() : null)
                .isFree(entity.getIsFree())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<EventResponse> toEventResponseList(List<Event> entities) {
        return entities.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    // Site Mapping
    public SiteResponse toSiteResponse(Site entity) {
        return SiteResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .capacity(entity.getCapacity())
                .pricePerNight(entity.getPricePerNight())
                .images(entity.getImages())
                .amenities(entity.getAmenities())
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .isActive(entity.getIsActive())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(entity.getOwner() != null ? entity.getOwner().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    // Achievement Mapping
    public AchievementResponse toAchievementResponse(Achievement entity) {
        return AchievementResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .badge(entity.getBadge())
                .icon(entity.getIcon())
                .requiredPoints(entity.getRequiredPoints())
                .rewardPoints(entity.getRewardPoints())
                .category(entity.getCategory())
                .level(entity.getLevel())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<AchievementResponse> toAchievementResponseList(List<Achievement> entities) {
        return entities.stream().map(this::toAchievementResponse).collect(Collectors.toList());
    }
    // Order Mapping
    public OrderResponse toOrderResponse(Order entity) {
        return OrderResponse.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .subtotal(entity.getSubtotal())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    // Promotion Mapping
    public PromotionResponse toPromotionResponse(Promotion entity) {
        return PromotionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .discountValue(entity.getDiscountValue())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<PromotionResponse> toPromotionResponseList(List<Promotion> entities) {
        return entities.stream().map(this::toPromotionResponse).collect(Collectors.toList());
    }

    // Reservation Mapping
    public ReservationResponse toReservationResponse(Reservation entity) {
        return ReservationResponse.builder()
                .id(entity.getId())
                .reservationNumber(entity.getReservationNumber())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .checkInDate(entity.getCheckInDate() != null ? entity.getCheckInDate().toLocalDate() : null)
                .checkOutDate(entity.getCheckOutDate() != null ? entity.getCheckOutDate().toLocalDate() : null)
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<ReservationResponse> toReservationResponseList(List<Reservation> entities) {
        return entities.stream().map(this::toReservationResponse).collect(Collectors.toList());
    }

    // Sponsor Mapping
    public List<SponsorResponse> toSponsorResponseList(List<Sponsor> entities) {
        return entities.stream().map(this::toSponsorResponse).collect(Collectors.toList());
    }
    // Cart Mapping
    public CartResponse toCartResponse(Cart entity) {
        return CartResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .items(toCartItemResponseList(entity.getItems()))
                .totalAmount(entity.getTotalAmount())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem entity) {
        return CartItemResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .productThumbnail(entity.getProduct() != null ? entity.getProduct().getThumbnail() : null)
                .quantity(entity.getQuantity())
                .unitPrice(entity.getPrice())
                .build();
    }

    public List<CartItemResponse> toCartItemResponseList(List<CartItem> entities) {
    }

    // CampingService Mapping
    public CampingServiceResponse toCampingServiceResponse(CampingService entity) {
        return CampingServiceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .price(entity.getPrice())
                .isActive(entity.getIsActive())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<CampingServiceResponse> toCampingServiceResponseList(List<CampingService> entities) {
        return entities.stream().map(this::toCampingServiceResponse).collect(Collectors.toList());
    }
    // Alert Mapping
    public AlertResponse toAlertResponse(Alert entity) {
        return AlertResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .alertType(entity.getAlertType())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<AlertResponse> toAlertResponseList(List<Alert> entities) {
        return entities.stream().map(this::toAlertResponse).collect(Collectors.toList());
    }

    // Wallet Mapping
    public WalletResponse toWalletResponse(Wallet entity) {
        return WalletResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}