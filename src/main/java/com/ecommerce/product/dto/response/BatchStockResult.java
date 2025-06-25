package com.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class BatchStockResult {

    private UUID productUUID;

    private boolean isSuccessAll;

    private List<StockDeductResult> resultList;

    public boolean hasFailed() {
        boolean anyFailed = resultList.stream().anyMatch(StockDeductResult::hasFailed);
        return resultList == null || resultList.isEmpty() || anyFailed;
    }

    public List<StockDeductResult> getFailedList() {
        return resultList.stream().filter(StockDeductResult::hasFailed).collect(Collectors.toList());
    }

}
