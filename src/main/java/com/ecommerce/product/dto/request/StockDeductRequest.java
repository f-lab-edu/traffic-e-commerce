package com.ecommerce.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductRequest {

    private UUID productUUID;

    private Integer quantity;

}
