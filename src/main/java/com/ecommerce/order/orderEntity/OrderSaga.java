package com.ecommerce.order.orderEntity;

import com.ecommerce.order.status.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_sagas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSaga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID orderUUID;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_status", nullable = false)
    private SagaStatus sagaStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private SagaStep currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    private ProductStatus productStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status")
    private ShipmentStatus shipmentStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    // 사가 상태 전환 메서드 : 재고 확인 요청 시작
    public void requestStockQtyChecked() {
        this.currentStep = SagaStep.STOCK;
    }

    public void execStockDeduction() {
        this.currentStep = SagaStep.STOCK;
    }

    public void completeStockDeduction() {
        this.productStatus = ProductStatus.DEDUCTED;
    }

    // 사가 상태 전환 메서드 : 재고 부족, 결제 환불
    public void failStockDeduction() {
        this.productStatus = ProductStatus.INSUFFICIENT;
        this.currentStep = SagaStep.COMPENSATING;
    }

    // 사가 상태 전환 메서드: 결제 요청 시작
    public void startPayment() {
        this.currentStep = SagaStep.PAYMENT;
        this.sagaStatus = SagaStatus.IN_PROGRESS;
    }

    public void completePayment() {
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    public void failPayment() {
        this.paymentStatus = PaymentStatus.FAILED;
        this.sagaStatus = SagaStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }

    // 사가 상태 전환 메서드 : 배송 요청 시작
    public void startShipment() {
        this.currentStep = SagaStep.SHIPMENT;
    }

    public void completeShipment() {
        this.shipmentStatus = ShipmentStatus.CREATED;
        this.sagaStatus = SagaStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 사가 상태 전환 메서드 : 결제 환불
    public void execCompensation() {
        this.currentStep = SagaStep.COMPENSATING;
        this.sagaStatus = SagaStatus.COMPENSATING;
    }

    public void completeCompensation() {
        this.sagaStatus = SagaStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < 3; // 최대 3회 재시도
    }

}
