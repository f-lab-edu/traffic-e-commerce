package com.ecommerce.payment.event.consumer;

import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.paymenrService.PaymentService;
import com.ecommerce.proto.EdaMessage;
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

    @KafkaListener(topics = "order-events", groupId = "payment-service")
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
            log.error("Payment error : consume order event {}", e.getMessage());
        }
    }

    private void operateOrderCreated(byte[] eventBytes) {
        try {
            EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(eventBytes);

            UUID orderUUID = UUID.fromString(event.getOrderUUID());
            BigDecimal totalPrice = BigDecimal.valueOf(event.getTotalPrice());

            // 결제 대기 상태 생성 - 실제 결제 정보는 추후 사용자 입력으로 받음
            paymentService.createPendingPayment(orderUUID, totalPrice);
        } catch (InvalidProtocolBufferException e) {
            log.error("ProtoBuf parse error : {}", e.getMessage());
        } catch (Exception e) {
            log.error("created Order  error : {}", e.getMessage());
        }


    }

    private void operateOrderCancelled(byte[] eventBytes) {
        try {
            EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(eventBytes);

            UUID orderUUID = UUID.fromString(event.getOrderUUID());

            paymentService.cancelPaymentsByOrderUUID(orderUUID);
        } catch (InvalidProtocolBufferException e) {
            log.error("ProtoBuf parse error : {}", e.getMessage());
        } catch (Exception e) {
            log.error("Canceled Order  error : {}", e.getMessage());
        }


    }

}
