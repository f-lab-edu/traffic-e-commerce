package com.ecommerce.payment.dto.request;

import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private UUID orderUUID;

    @NotNull
    private BigDecimal amount;

    private String paymentMethod;

    private String cardNumber;

    private String cardHolderName;

    private String expiryDate;

    private String cvv;

}
