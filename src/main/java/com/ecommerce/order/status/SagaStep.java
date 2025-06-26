package com.ecommerce.order.status;

public enum SagaStep {
    PAYMENT,        // 결제 단계
    INVENTORY,      // 재고 차감 단계
    SHIPMENT,       // 배송 단계
    COMPENSATING    // 보상 단계
}
