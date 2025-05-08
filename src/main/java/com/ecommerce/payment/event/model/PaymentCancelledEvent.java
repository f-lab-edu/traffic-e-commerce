package com.ecommerce.payment.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelledEvent {

    private UUID orderUUID;

    private Long paymentId;

    private BigDecimal amount;

    private String cancelReason;

    private LocalDateTime canceledDt;

}
