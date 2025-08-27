package com.ecommerce.order.event.saga;

import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderService.OrderSagaOrchestrator;
import com.ecommerce.proto.*;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaEventConsumer {

    private final OrderSagaOrchestrator orchestrator;

    @KafkaListener(topics = "order-events", groupId = "order-service")
    public void consumeOrderEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();

            switch (key) {
                case "order.created": operateOrderEvent(value);
                case "order.cancelled": cancelOrderEvent(value);
                default: break;
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Consume order error : {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "product-events", groupId = "product-service")
    public void consumeProductEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();

            switch (key) {
                case "stock.check.response":
                case "stock.deducted":      operateProductDeductedEvent(value);
                case "stock.lacked":        operateProductLackedEvent(value);
                case "stock.restored":      operateStockRestoredEvent(value);
                case "stock.bulk.deducted": operateStockBulkDeductedEvent(value);
                default:
            }

        } catch (Exception e) {
            log.error("[Orchestrator] Consume product error : {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "payment-events", groupId = "payment-service")
    public void consumePaymentEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();

            switch (key) {
                case "payment.success":      operatePaymentSuccess(value);
                case "payment.failed":       operatePaymentFailed(value);
                case "payment.cancelled":    operatePaymentCancelled(value);
                default:
            }

        } catch (Exception e) {
            log.error("[Orchestrator] Consume payment error : {}", e.getMessage());
        }
    }


    private void operatePaymentSuccess(byte[] value) {
        try {
            PaymentSuccessEvent paymentSuccessed = PaymentSuccessEvent.parseFrom(value);
            orchestrator.handlePaymentSuccess(paymentSuccessed);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

        ;
    }

    private void operatePaymentFailed(byte[] value) {
        try {
            PaymentFailedEvent paymentFailed = PaymentFailedEvent.parseFrom(value);
            orchestrator.handlePaymentFailed(paymentFailed);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

    }

    private void operatePaymentCancelled(byte[] value) {
        try {
            PaymentCancelledEvent paymentCancelled = PaymentCancelledEvent.parseFrom(value);
            orchestrator.handlePaymentCancelled(paymentCancelled);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = "shipment-events", groupId = "shipment-service")
    public void consumeShipmentEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();

            operateShipmentEvent(value);

        } catch (Exception e) {
            log.error("[Orchestrator] Consume Shipment error : {}", e.getMessage());
        }
    }

    private void operateOrderEvent(byte[] value) {
        Order orderCreated = parseOrderCreated(value);
        orchestrator.startOrderSaga(orderCreated);
    }

    private void cancelOrderEvent(byte[] value) {
        UUID cancelledUUID = parseOrderCancelled(value);
        orchestrator.cancelOrderSaga(cancelledUUID);
    }


    private void operateShipmentEvent(byte[] eventBytes) {

    }

    private Order parseOrderCreated(byte[] eventBytes) {
        try {
            OrderCreatedEvent orderCreated = OrderCreatedEvent.parseFrom(eventBytes);
            return Order.of(orderCreated);
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing order create error : {}", e.getMessage());
            throw new RuntimeException("Failed to create order event", e);
        }
    }


    private UUID parseOrderCancelled(byte[] eventBytes) {
        try {
            OrderCancelledEvent orderCancelled = OrderCancelledEvent.parseFrom(eventBytes);
            return UUID.fromString(orderCancelled.getOrderUuid());
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing order cancelled error : {}", e.getMessage());
            throw new RuntimeException("Failed to create order event", e);
        }
    }

    private void operateProductDeductedEvent(byte[] value) {
        StockDeductEvent stockDeducted = parseStockDeducted(value);
        orchestrator.handleStockDeductSuccess(stockDeducted);
    }

    private void operateProductLackedEvent(byte[] value) {
        StockDeductLackedEvent stockLacked = parseStockLacked(value);
        orchestrator.handleStockLacked(stockLacked);
    }

    private void operateStockRestoredEvent(byte[] value) {
        StockRestoredEvent stockRestored = parseStockRestored(value);
        orchestrator.handleStockRestored(stockRestored);
    }

    private void operateStockBulkDeductedEvent(byte[] value) {
        StockDeductEvent stockBulkDeducted = parseStockBulkDeducted(value);
        orchestrator.handleStockBulkDeducted(stockBulkDeducted);
    }


    private StockDeductEvent parseStockDeducted(byte[] eventBytes) {
        try {
            return StockDeductEvent.parseFrom(eventBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing deducted error : {}", e.getMessage());
            throw new RuntimeException("Failed to deduct stock event", e);
        }
    }

    private StockDeductLackedEvent parseStockLacked(byte[] eventBytes) {
        try {
            return StockDeductLackedEvent.parseFrom(eventBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing lacked error : {}", e.getMessage());
            throw new RuntimeException("Failed to lack stock event", e);
        }
    }


    private StockRestoredEvent parseStockRestored(byte[] eventBytes) {
        try {
            return StockRestoredEvent.parseFrom(eventBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing restored error : {}", e.getMessage());
            throw new RuntimeException("Failed to restore stock event", e);
        }
    }

    private StockDeductEvent parseStockBulkDeducted(byte[] eventBytes) {
        try {
            return StockDeductEvent.parseFrom(eventBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("[Orchestrator] parsing bulk deduct error : {}", e.getMessage());
            throw new RuntimeException("Failed to deduct bulk stock event", e);
        }
    }

}
