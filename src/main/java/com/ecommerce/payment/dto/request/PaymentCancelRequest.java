package com.ecommerce.payment.dto.request;

import com.ecommerce.payment.paymentDomain.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaymentCancelRequest {

    private Long paymentId;

    private UUID transactionId;

    private BigDecimal amount;

    private String reason;

    public static PaymentCancelRequest of(Payment request) {
        return PaymentCancelRequest.builder().transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .reason(request.getCancelReason())
                .build();
    }

}
