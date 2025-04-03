package com.ecommerce.product.dto.request;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductModifyRequest {

    private String name;

    private String description;

    private BigDecimal price;

    private int stockQuantity;

    private List<String> imageUrls;

    private String status;

}
