package com.ecommerce.product.productEntity;

import com.ecommerce.product.dto.request.ProductRegisterRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> imageList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory categoryId;


    @Column(name = "crea_dt")
    private LocalDateTime creaDt;

    @Column(name = "updt_dt")
    private LocalDateTime updtDt;

    public static Product of(Long sellerId, ProductRegisterRequest dto, ProductCategory category) {
        return Product.builder()
                .sellerId(sellerId)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .categoryId(category)
                .status(ProductStatus.ACTIVE)
                .build();


    }

    public static Product of(Product savedProduct, List<ProductImage> imageList) {
        return Product.builder()
                .sellerId(savedProduct.sellerId)
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .stockQuantity(savedProduct.getStockQuantity())
                .status(savedProduct.getStatus())
                .imageList(imageList)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
        this.updtDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updtDt = LocalDateTime.now();
    }

}
