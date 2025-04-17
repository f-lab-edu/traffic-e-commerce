package com.ecommerce.product.productRepository;

import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {


    void deleteByProduct(Product product);

//    void saveAll(List<ProductImage> productImage );
}
