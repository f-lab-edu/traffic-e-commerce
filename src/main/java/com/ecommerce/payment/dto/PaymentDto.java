package com.ecommerce.payment.dto;

import com.ecommerce.payment.paymentDomain.Payment;
import com.ecommerce.payment.paymentDomain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;

    private UUID orderUUID;

    private BigDecimal amount;

    private PaymentStatus status;

    private String paymentMethod;

    private UUID transactionId;

    private LocalDateTime creaDt;

    private LocalDateTime updtDt;

    private LocalDateTime completedDt;

    private LocalDateTime failedDt;

    private LocalDateTime cancelledDt;

    private String failureReason;

    private String cancelReason;

}
