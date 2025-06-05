package com.ecommerce.payment.paymentDomain;

import com.ecommerce.payment.dto.request.PaymentRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_uuid", columnDefinition = "BINARY(16)", nullable = false)
    private UUID orderUUID;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = true)
    private UUID transactionId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creaDt;

    @LastModifiedDate
    private LocalDateTime updtDt;

    private LocalDateTime completedDt;

    private LocalDateTime failedDt;

    private LocalDateTime cancelledDt;

    private String failureReason;

    private String cancelReason;

    public static Payment of(PaymentRequest request) {
        return Payment.builder()
                .orderUUID(request.getOrderUUID())  // UUID 사용
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public static Payment of(UUID orderUUID, BigDecimal amount) {
        return Payment.builder()
                .orderUUID(orderUUID)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();
    }



    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
        this.updtDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updtDt = LocalDateTime.now();
    }

    public void completePayment(UUID transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.completedDt = LocalDateTime.now();
    }


    public void failPayment(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.failedDt = LocalDateTime.now();
    }

    public void cancelPayment(String cancelReason) {
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledDt = LocalDateTime.now();
    }

    public void savePaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }



}
