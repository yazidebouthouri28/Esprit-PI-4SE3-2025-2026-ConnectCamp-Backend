package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String icon;
    private String image;
    private Long parentId;
    private String parentName;
    private Integer displayOrder;
    private Integer productCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }

    public String getIcon() {
        return icon;
    }

    public String getImage() {
        return image;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static CategoryResponseBuilder builder() {
        return new CategoryResponseBuilder();
    }

    public static class CategoryResponseBuilder {
        private CategoryResponse response = new CategoryResponse();

        public CategoryResponseBuilder id(Long id) {
            response.id = id;
            return this;
        }

        public CategoryResponseBuilder name(String name) {
            response.name = name;
            return this;
        }

        public CategoryResponseBuilder description(String description) {
            response.description = description;
            return this;
        }

        public CategoryResponseBuilder slug(String slug) {
            response.slug = slug;
            return this;
        }

        public CategoryResponseBuilder icon(String icon) {
            response.icon = icon;
            return this;
        }

        public CategoryResponseBuilder image(String image) {
            response.image = image;
            return this;
        }

        public CategoryResponseBuilder parentId(Long parentId) {
            response.parentId = parentId;
            return this;
        }

        public CategoryResponseBuilder parentName(String parentName) {
            response.parentName = parentName;
            return this;
        }

        public CategoryResponseBuilder displayOrder(Integer displayOrder) {
            response.displayOrder = displayOrder;
            return this;
        }

        public CategoryResponseBuilder productCount(Integer productCount) {
            response.productCount = productCount;
            return this;
        }

        public CategoryResponseBuilder isActive(Boolean isActive) {
            response.isActive = isActive;
            return this;
        }

        public CategoryResponseBuilder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public CategoryResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public CategoryResponse build() {
            return response;
        }
    }
}
