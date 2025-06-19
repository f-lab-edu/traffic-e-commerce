package com.ecommerce.payment.paymentController;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.dto.request.PaymentCancelRequest;
import com.ecommerce.payment.dto.request.PaymentProcessRequest;
import com.ecommerce.payment.paymenrService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 주문에 대한 결제 정보 제출
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(@Validated @RequestBody PaymentProcessRequest request) {
        return ResponseEntity.ok(paymentService.processPaymentWithDetails(request));
    }

    /**
     * 결제 상태 조회
     */
    @GetMapping("/order/{orderUUID}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByOrderUUID(@PathVariable UUID orderUUID) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderUUID(orderUUID));
    }

    /**
     * 결제 취소 요청
     */
    @PostMapping("/cancel")
    public ResponseEntity<PaymentDto> cancelPayment(@Validated @RequestBody PaymentCancelRequest request) {
        return ResponseEntity.ok(paymentService.cancelPayment(request.getPaymentId(), request.getReason()));
    }

}
