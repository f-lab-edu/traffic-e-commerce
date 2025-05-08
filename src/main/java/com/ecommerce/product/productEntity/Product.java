package com.ecommerce.product.productEntity;

import com.ecommerce.product.dto.request.ProductRegisterRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "product_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID productUUID;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> imageList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "crea_dt")
    private LocalDateTime creaDt;

    @Column(name = "updt_dt")
    private LocalDateTime updtDt;

    public static Product of(UUID sellerId, ProductRegisterRequest dto, ProductCategory category) {
        return Product.builder()
                .sellerId(sellerId)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(category)
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    public static Product of(Product savedProduct, List<ProductImage> imageList) {
        return Product.builder()
                .sellerId(savedProduct.sellerId)
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .stockQuantity(savedProduct.getStockQuantity())
                .isActive(savedProduct.isActive)
                .isDeleted(savedProduct.isDeleted)
                .imageList(imageList)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
        this.updtDt = LocalDateTime.now();
        this.productUUID = UUID.randomUUID();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updtDt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isActive = false;
        this.isDeleted = true;
    }

}
