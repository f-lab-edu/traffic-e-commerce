package com.ecommerce.payment.dto.request;

import com.ecommerce.payment.paymentDomain.Payment;
import com.ecommerce.payment.paymentDomain.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayRequest {

    private UUID orderUUID;

    private String paymentMethod; // CARD, VIRTUAL_ACCOUNT

    private BigDecimal amount;

    // 카드 결제 정보들
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    // 가상계좌 결제 관련 정보
    private String bankCode;
    private String accountHolder;


    public static PaymentGatewayRequest of(PaymentRequest request) {
        return PaymentGatewayRequest.builder()
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .cardNumber(request.getCardNumber())
                .cardHolderName(request.getCardHolderName())
                .expiryDate(request.getExpiryDate())
                .cvv(request.getCvv())
                .orderUUID(request.getOrderUUID())  // UUID 사용
                .build();
    }

    public static PaymentGatewayRequest createGatewayRequest(Payment payment) {
        return PaymentGatewayRequest.builder()
                .orderUUID(payment.getOrderUUID())
                .amount(payment.getAmount())
                .build();

    }
}
