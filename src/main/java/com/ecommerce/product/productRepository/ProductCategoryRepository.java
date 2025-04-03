package com.ecommerce.product.productRepository;

import com.ecommerce.product.productEntity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByName(String name);

    ProductCategory save(ProductCategory category);

}
