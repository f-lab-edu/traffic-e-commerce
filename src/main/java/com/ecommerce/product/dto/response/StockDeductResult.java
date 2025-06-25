package com.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class StockDeductResult {

    private UUID productUUID;

    private boolean success;

    private Integer remainingStock;



    public static StockDeductResult success(UUID productUUID, Integer remainingStock) {
        return StockDeductResult.builder()
                .productUUID(productUUID)
                .success(true)
                .remainingStock(remainingStock)
                .build();
    }

    public static StockDeductResult failed(UUID productUUID) {
        return StockDeductResult.builder()
                .productUUID(productUUID)
                .success(false)
                .build();
    }

    public boolean hasFailed() {
        return !this.success;
    }

}
