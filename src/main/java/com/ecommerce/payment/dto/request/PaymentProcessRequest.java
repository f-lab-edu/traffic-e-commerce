package com.ecommerce.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessRequest {


    private UUID orderUUID;

    private String paymentMethod; // CARD, VIRTUAL_ACCOUNT

    // 카드 결제 정보들
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    // 가상계좌 결제 관련 정보
    private String bankCode;
    private String accountHolder;

}
