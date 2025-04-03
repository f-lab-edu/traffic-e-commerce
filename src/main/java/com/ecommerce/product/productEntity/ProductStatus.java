package com.ecommerce.product.productEntity;

public enum ProductStatus {
    ACTIVE("ACTIVE"),
    HIDDEN("HIDDEN"),
    DELETED("DELETED");

    private String statusCode;

    ProductStatus(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return this.statusCode;
    }
}
