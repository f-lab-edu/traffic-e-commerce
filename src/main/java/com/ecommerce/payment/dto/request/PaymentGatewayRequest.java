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

    public static PaymentGatewayRequest createGatewayRequest(Payment payment, PaymentProcessRequest request) {
        PaymentGatewayRequest.PaymentGatewayRequestBuilder builder = PaymentGatewayRequest.builder()
                .orderUUID(payment.getOrderUUID())
                .amount(payment.getAmount())
                .paymentMethod(request.getPaymentMethod());

        // 결제 방법에 따라 필요한 정보 추가
        if (PaymentMethod.CARD.equals(request.getPaymentMethod())) {
            builder.cardNumber(request.getCardNumber())
                    .cardHolderName(request.getCardHolderName())
                    .expiryDate(request.getExpiryDate())
                    .cvv(request.getCvv());
        } else if (PaymentMethod.VIRTUAL_ACCOUNT.equals(request.getPaymentMethod())) {
            builder.bankCode(request.getBankCode())
                    .accountHolder(request.getAccountHolder());
        }

        return builder.build();
    }

}
