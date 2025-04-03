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
        return new ProductResponse(
                product.getId()
                , product.getName()
                , product.getPrice()
                , product.getDescription()
                , product.getImageList().stream().map(ProductImage::getImageUrl).collect(Collectors.toList())
        );
    }

}
