package com.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class BulkStockDeductResult {

    private UUID orderUUID;
    private boolean allSuccess;
    private List<StockDeductResult> results;

    public boolean hasFailed() {
        boolean anyFailed = results.stream().anyMatch(StockDeductResult::hasFailed);
        return results != null && !results.isEmpty() || anyFailed;
    }



}
