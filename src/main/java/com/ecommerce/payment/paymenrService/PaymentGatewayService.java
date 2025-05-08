package com.ecommerce.payment.paymenrService;

import com.ecommerce.payment.dto.request.PaymentCancelRequest;
import com.ecommerce.payment.dto.request.PaymentGatewayRequest;
import com.ecommerce.payment.dto.response.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PaymentGatewayService {

    private final double SUCCESS_RATE = 0.9;

    /**
     * 결제 요청을 처리하는 메서드
     */
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {

        // 거래 ID 생성
        UUID transactionId = UUID.randomUUID();

        // 결제 성공 여부를 확률적으로 결정 (90% 성공)
        boolean isSuccess = Math.random() < SUCCESS_RATE;

        // 실제 PG 서비스처럼 약간의 지연 시간 추가
        try {
            Thread.sleep((long) (Math.random() * 1000) + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 결제 응답 생성
        return PaymentGatewayResponse.of(isSuccess, transactionId, request.getAmount());
    }

    public PaymentGatewayResponse cancelPayment(PaymentCancelRequest request) {

        // 취소 확률
        boolean isSuccess = Math.random() < SUCCESS_RATE;

        // 지연 시간 추가
        try {
            Thread.sleep((long) (Math.random() * 800) + 300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return PaymentGatewayResponse.of(isSuccess, request.getTransactionId(), request.getAmount());

    }


}
