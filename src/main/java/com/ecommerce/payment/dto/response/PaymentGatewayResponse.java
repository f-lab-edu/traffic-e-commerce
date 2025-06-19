package com.ecommerce.payment.dto.response;

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
public class PaymentGatewayResponse {

    private boolean success;

    private UUID transactionId;

    private BigDecimal amount;

    private String message;

    private LocalDateTime processingTime;

    public static PaymentGatewayResponse of(boolean isSuccess, UUID transactionId, BigDecimal amount) {
        return PaymentGatewayResponse.builder()
                .success(isSuccess)
                .transactionId(transactionId)
                .amount(amount)
                .message(responseMessage(isSuccess))
                .processingTime(LocalDateTime.now())
                .build();
    }

    private static String responseMessage(boolean isSuccess) {
        return isSuccess ? "Payment completed successfully." : "Error encountered on payment.";
    }

    public boolean isSuccess() {
        return success;
    }

}
