package com.ecommerce.product.productEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "crea_dt", nullable = false)
    private LocalDateTime creaDt;

    public static ProductCategory of(String name) {
        return ProductCategory.builder().name(name).build();
    }

    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
    }


}
