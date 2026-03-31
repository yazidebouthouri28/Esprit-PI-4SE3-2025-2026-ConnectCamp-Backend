package tn.esprit.productservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_seller",   columnList = "seller_id"),
        @Index(name = "idx_product_active",   columnList = "is_active"),
        @Index(name = "idx_product_price",    columnList = "price"),
        @Index(name = "idx_product_sku",      columnList = "sku")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 200)
    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    @Size(max = 2000)
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "9999999.99")
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @DecimalMin(value = "0.00")
    @Column(precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Size(max = 50)
    @Column(unique = true)
    private String sku;

    @Size(max = 50)
    private String barcode;

    @Size(max = 100)
    private String brand;

    // FIX : category est LAZY, @JsonIgnoreProperties casse la boucle
    // Product -> category -> products (ignoré) -> OK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"subcategories", "products", "parent", "hibernateLazyInitializer", "handler"})
    private Category category;

    @Column(name = "seller_id")
    private UUID sellerId;

    @Min(value = 0)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Min(value = 0)
    @Builder.Default
    private Integer minStockLevel = 5;

    @Min(value = 1)
    @Builder.Default
    private Integer maxStockLevel = 1000;

    @Builder.Default
    private Boolean trackInventory = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Size(max = 500)
    private String thumbnail;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "5.00")
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Min(value = 0)
    @Builder.Default
    private Integer reviewCount = 0;

    @Min(value = 0)
    @Builder.Default
    private Integer salesCount = 0;

    @Min(value = 0)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Boolean isOnSale = false;

    @Builder.Default
    private Boolean isRentable = false;

    @DecimalMin(value = "0.00")
    @Column(precision = 15, scale = 2)
    private BigDecimal rentalPricePerDay;

    @DecimalMin(value = "0.00")
    private Double weight;

    @Size(max = 100)
    private String dimensions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // FIX : @JsonIgnore sur reviews dans Product pour casser la boucle
    // Product -> reviews -> ProductReview -> product (ignoré) -> OK
    // Les reviews sont exposées via leur propre endpoint, pas via Product directement
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (stockQuantity == null) stockQuantity = 0;
        if (reviewCount == null)   reviewCount = 0;
        if (salesCount == null)    salesCount = 0;
        if (viewCount == null)     viewCount = 0;
        if (rating == null)        rating = BigDecimal.ZERO;
        if (isActive == null)      isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}