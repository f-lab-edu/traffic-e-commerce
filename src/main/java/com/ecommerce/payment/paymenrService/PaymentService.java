package com.ecommerce.payment.paymenrService;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.dto.request.PaymentCancelRequest;
import com.ecommerce.payment.dto.request.PaymentGatewayRequest;
import com.ecommerce.payment.dto.response.PaymentGatewayResponse;
import com.ecommerce.payment.event.producer.PaymentEventProducer;
import com.ecommerce.payment.paymentDomain.Payment;
import com.ecommerce.payment.paymentDomain.PaymentStatus;
import com.ecommerce.payment.paymentRepository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentGatewayService gatewayService;


    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByOrderUUID(UUID orderUUID) {
        List<Payment> payments = paymentRepository.findByOrderUUID(orderUUID);
        return payments.stream().map(this::convertToDto).toList();
    }


    // 주문 생성 이벤트 수신 시 호출 - 결제 대기 상태 생성
    @Transactional
    public void createPendingPayment(UUID orderUUID, BigDecimal amount) {
        Payment payment = Payment.of(orderUUID, amount);

        // 결제 정보 조회
        Payment findPayment = paymentRepository.findByOrderUUIDAndStatus(payment.getOrderUUID(), PaymentStatus.PENDING);
        if (findPayment != null) {
            throw new EntityNotFoundException("duplicated pending payments");
        }

        Payment pendingPayment = paymentRepository.save(payment);
        processPaymentWithDetails(pendingPayment);
    }


    // 실제 결제 처리
    @Transactional
    public void processPaymentWithDetails(Payment payment) {
        // 결제 게이트웨이 요청 생성
        PaymentGatewayRequest gatewayRequest = PaymentGatewayRequest.createGatewayRequest(payment);

        // 결제 게이트웨이 호출
        PaymentGatewayResponse gatewayResponse = gatewayService.processPayment(gatewayRequest);

        // 결제 결과 처리
        if (gatewayResponse.isSuccess()) {
            payment.completePayment(gatewayResponse.getTransactionId());
            Payment updatedPayment = paymentRepository.save(payment);
            paymentEventProducer.publishPaymentSuccess(updatedPayment);
        } else {
            payment.failPayment(gatewayResponse.getMessage());
            Payment updatedPayment = paymentRepository.save(payment);
            paymentEventProducer.publishPaymentFailed(updatedPayment);
        }
    }

    // 주문 취소로 자동 결제 취소 : UUID만 받는 오버로드 cancel 메서드
    public void cancelPaymentsByOrderUUID(UUID orderUUID) {

        String defaultReason = "Order event : cancel";
        cancelPaymentsByOrderUUID(orderUUID, defaultReason);
    }


    public void cancelPaymentsByOrderUUID(UUID orderUUID, String reason) {
        List<Payment> payments = paymentRepository.findByOrderUUID(orderUUID);

        for (Payment payment : payments) {
            if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
                try {
                    cancelPayment(payment.getId(), reason);
                } catch (Exception e) {
                    log.error("Error encounter on cancelling payment : {}", e.getMessage());
                }
            }
        }

    }


    // 결제 취소
    @Transactional
    public void cancelPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("Cannot find payment info"));

        if (!payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new IllegalStateException("Only able to cancel payment succeed, status : " + payment.getStatus());
        }

        PaymentCancelRequest cancelRequest = PaymentCancelRequest.of(payment);
        PaymentGatewayResponse gatewayResponse = gatewayService.cancelPayment(cancelRequest);

        if (gatewayResponse.isSuccess()) {
            // 결제 취소 처리
            payment.cancelPayment(reason);
            Payment updatedPayment = paymentRepository.save(payment);

            paymentEventProducer.publishPaymentCancelled(updatedPayment);
            log.info("Finish cancelling payment : payment ID {}, order ID {}", updatedPayment.getId(), updatedPayment.getOrderUUID());

        } else {
            log.error("Fail cancelling payment: payment ID {}", payment.getId());
            throw new RuntimeException("Error encounters on cancelling payment " + gatewayResponse.getMessage());
        }
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderUUID(payment.getOrderUUID())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .creaDt(payment.getCreaDt())
                .updtDt(payment.getUpdtDt())
                .completedDt(payment.getCompletedDt())
                .failedDt(payment.getFailedDt())
                .cancelledDt(payment.getCancelledDt())
                .failureReason(payment.getFailureReason())
                .build();
    }

}
