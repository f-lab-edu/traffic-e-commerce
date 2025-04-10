package com.ecommerce.product.dto.response;

import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductImage;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class ProductResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private List<String> imageUrls;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrls(product.getImageList().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()))
                .build();
    }

}
