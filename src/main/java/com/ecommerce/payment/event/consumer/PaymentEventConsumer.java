package com.ecommerce.payment.event.consumer;

import com.ecommerce.payment.paymenrService.PaymentService;
import com.ecommerce.proto.PaymentRequestedEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "saga-events", groupId = "payment-service")
    public void consumeOrderEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();
            log.info("Payment consume: order key={}", key);
            if ("order.created".equals(key)) {
                operateOrderCreated(value);
            } else if ("order.cancelled".equals(key)) {
                operateOrderCancelled(value);
            }

        } catch (Exception e) {
            log.error("[Payment] consume error : order event {}", e.getMessage());
        }
    }

    private void operateOrderCreated(byte[] eventBytes) {
        try {
            UUID orderUUID = parserUUID(eventBytes);
            BigDecimal totalPrice = parsePurchasedAmount(eventBytes);

            // 결제 대기 상태 생성 > PG 결제 요청
            paymentService.createPendingPayment(orderUUID, totalPrice);
        } catch (Exception e) {
            log.error("[Payment] created Order  error : {}", e.getMessage());
        }
    }

    private void operateOrderCancelled(byte[] eventBytes) {
        try {
            UUID orderUUID = parserUUID(eventBytes);
            paymentService.cancelPaymentsByOrderUUID(orderUUID);
        } catch (Exception e) {
            log.error("[Payment] Canceled Order  error : {}", e.getMessage());
        }
    }

    private UUID parserUUID(byte[] eventBytes) {
        try {
            PaymentRequestedEvent event = PaymentRequestedEvent.parseFrom(eventBytes);
            return UUID.fromString(event.getOrderUuid());
        } catch (InvalidProtocolBufferException e) {
            log.error("[Payment] parsing UUID error : {}", e.getMessage());
            throw new RuntimeException("Failed to parse order UUID", e);
        }
    }

    private BigDecimal parsePurchasedAmount(byte[] eventBytes) {
        try {
            PaymentRequestedEvent event = PaymentRequestedEvent.parseFrom(eventBytes);
            return BigDecimal.valueOf(event.getAmount());
        } catch (InvalidProtocolBufferException e) {
            log.error("[Payment] parsing purchased amount error : {}", e.getMessage());
            throw new RuntimeException("Failed to parse order UUID", e);
        }
    }

}
