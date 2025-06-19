package com.ecommerce.product.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class StockDeductRequest {

    private UUID productUUID;

    private Integer quantity;

}
