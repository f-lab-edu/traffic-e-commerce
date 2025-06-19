package com.ecommerce.product.productEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Product 테이블의 정수값 pk와 조인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "crea_dt", nullable = false)
    private LocalDateTime creaDt;

    public static ProductImage of(Product savedProduct, String url) {
        return ProductImage.builder()
                .product(savedProduct)
                .imageUrl(url)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isActive = false;
        this.isDeleted = true;
    }
}
