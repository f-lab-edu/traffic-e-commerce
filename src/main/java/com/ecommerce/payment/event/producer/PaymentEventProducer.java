package com.ecommerce.payment.event.producer;

import com.ecommerce.payment.paymentDomain.Payment;
import com.ecommerce.proto.PaymentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void publishPaymentSuccess(Payment payment) {
        PaymentMessage.PaymentSuccessEvent event = PaymentMessage.PaymentSuccessEvent.newBuilder()
                .setOrderUuid(payment.getOrderUUID().toString())
                .setPaymentId(payment.getId())
                .setAmount(payment.getAmount().doubleValue())
                .setTransactionId(payment.getTransactionId().toString())
                .setCompletedDt(payment.getCompletedDt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();


        byte[] eventBytes = event.toByteArray();
        kafkaTemplate.send("payment-events", "payment.success", eventBytes);
    }

    public void publishPaymentFailed(Payment payment) {
        PaymentMessage.PaymentFailedEvent event = PaymentMessage.PaymentFailedEvent.newBuilder()
                .setOrderUuid(payment.getOrderUUID().toString())
                .setPaymentId(payment.getId())
                .setAmount(payment.getAmount().doubleValue())
                .setFailureReason(payment.getFailureReason())
                .setFailedDt(payment.getFailedDt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();


        byte[] eventBytes = event.toByteArray();
        kafkaTemplate.send("payment-events", "payment.failed", eventBytes);
    }

    public void publishPaymentCancelled(Payment payment) {
        PaymentMessage.PaymentCancelledEvent event = PaymentMessage.PaymentCancelledEvent.newBuilder()
                .setOrderUuid(payment.getOrderUUID().toString())
                .setPaymentId(payment.getId())
                .setAmount(payment.getAmount().doubleValue())
                .setCancelReason(payment.getCancelReason())
                .setCancelledDt(payment.getCancelledDt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();


        byte[] eventBytes = event.toByteArray();
        kafkaTemplate.send("payment-events", "payment.cancelled", eventBytes);
    }

}
