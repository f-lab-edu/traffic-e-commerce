package com.ecommerce.order.status;

public enum SagaStatus {
    STARTED,        // 사가 시작
    IN_PROGRESS,    // 진행 중
    COMPLETED,      // 성공 완료
    FAILED,         // 실패
    COMPENSATING,   // 보상 트랜잭션 진행 중
    COMPENSATED     // 보상 완료
}
