package tn.esprit.projetintegre.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.projetintegre.dto.response.*;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.nadineentities.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    // Category Mapping
    public CategoryResponse toCategoryResponse(Category entity) {
        if (entity == null) return null;
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .icon(null)
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
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    // Product Mapping
    public ProductResponse toProductResponse(Product entity) {
        if (entity == null) return null;
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
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toProductResponse).collect(Collectors.toList());
    }



    // Transaction Mapping
    // Transaction Mapping (corrigée)
    public TransactionResponse toTransactionResponse(Transaction entity) {
        if (entity == null) return null;
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
                .userId(entity.getWallet() != null && entity.getWallet().getUser() != null ?
                        entity.getWallet().getUser().getId() : null)
                .createdAt(entity.getCreatedAt())
                // Supprimé car absent du DTO
                .build();
    }

    public List<TransactionResponse> toTransactionResponseList(List<Transaction> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }
    // Wallet Mapping (complétée)
    public List<WalletResponse> toWalletResponseList(List<Wallet> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toWalletResponse).collect(Collectors.toList());
    }

    // Wishlist Mapping
    public WishlistResponse toWishlistResponse(Wishlist entity) {
        if (entity == null) return null;
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
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toWishlistResponse).collect(Collectors.toList());
    }


    // Order Mapping
    public OrderResponse toOrderResponse(Order entity) {
        if (entity == null) return null;
        return OrderResponse.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .totalPrice(entity.getTotalAmount()) // Utilise totalAmount de l'entité pour totalPrice du DTO
                .subtotal(entity.getSubtotal())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .shippingCost(entity.getShippingCost())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .paymentMethod(entity.getPaymentMethod())
                .shippingAddress(entity.getShippingAddress())
                .shippingCity(entity.getShippingCity())
                .shippingCountry(entity.getShippingCountry())
                .shippingPhone(entity.getShippingPhone())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }


    public List<OrderResponse> toOrderResponseList(List<Order> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toOrderResponse).collect(Collectors.toList());
    }
    // Cart Mapping
    public CartResponse toCartResponse(Cart entity) {
        if (entity == null) return null;
        return CartResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .items(toCartItemResponseList(entity.getItems()))
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getTotalAmount() != null && entity.getDiscountAmount() != null ?
                        entity.getTotalAmount().subtract(entity.getDiscountAmount()) : entity.getTotalAmount())
                .totalItems(entity.getItems() != null ? entity.getItems().size() : 0)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }



    public CartItemResponse toCartItemResponse(CartItem entity) {
        if (entity == null) return null;
        return CartItemResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                // Correction : productImage devient productThumbnail
                .productThumbnail(entity.getProduct() != null ? entity.getProduct().getThumbnail() : null)
                .quantity(entity.getQuantity())
                // Correction : price devient unitPrice pour correspondre au DTO
                .unitPrice(entity.getPrice())
                // Calcul du sous-total
                .subtotal(entity.getPrice() != null && entity.getQuantity() != null ?
                        entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())) : java.math.BigDecimal.ZERO)
                // Ajout du stock disponible depuis le produit
                .stockAvailable(entity.getProduct() != null ? entity.getProduct().getStockQuantity() : 0)
                .build();
    }

    public List<CartItemResponse> toCartItemResponseList(List<CartItem> entities) {
        if (entities == null) return java.util.Collections.emptyList();
        return entities.stream().map(this::toCartItemResponse).collect(java.util.stream.Collectors.toList());
    }


    // Alert Mapping
    public AlertResponse toAlertResponse(Alert entity) {
        if (entity == null) return null;
        return AlertResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .alertType(entity.getAlertType())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .location(entity.getLocation())
                .reportedById(entity.getReportedBy() != null ? entity.getReportedBy().getId() : null)
                .reportedByName(entity.getReportedBy() != null ? entity.getReportedBy().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .resolvedById(entity.getResolvedBy() != null ? entity.getResolvedBy().getId() : null)
                .resolvedByName(entity.getResolvedBy() != null ? entity.getResolvedBy().getName() : null)
                .resolutionNotes(entity.getResolutionNotes())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<AlertResponse> toAlertResponseList(List<Alert> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toAlertResponse).collect(Collectors.toList());
    }

    // Wallet Mapping
    public WalletResponse toWalletResponse(Wallet entity) {
        if (entity == null) return null;
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

    // Inventory Mapping
    public InventoryResponse toInventoryResponse(Inventory entity) {
        if (entity == null) return null;
        return InventoryResponse.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .warehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : null)
                .warehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null)
                .quantity(entity.getQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .lowStockThreshold(entity.getLowStockThreshold())
                .safetyStock(entity.getSafetyStock())
                .reorderQuantity(entity.getReorderQuantity())
                .location(entity.getLocation())
                .aisle(entity.getAisle())
                .shelf(entity.getShelf())
                .bin(entity.getBin())
                .lastStockCheck(entity.getLastStockCheck())
                .lastRestocked(entity.getLastRestocked())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}