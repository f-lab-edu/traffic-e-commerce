package com.ecommerce.order.event;

import com.ecommerce.order.orderService.OrderService;
import com.ecommerce.proto.OrderCancelledEvent;
import com.ecommerce.proto.OrderCreatedEvent;
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
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "order-events", groupId = "order-service")
    public void consumeOrderEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();

            switch (key) {
                case "order.created": createOrderEvent(value);
                case "order.cancelled": cancelOrderEvent(value);
                default: break;
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Consume order error : {}", e.getMessage());
        }
    }

    private void createOrderEvent(byte[] value) {
        OrderCreatedEvent orderCreatedEvent = null;
        try {
            orderCreatedEvent = OrderCreatedEvent.parseFrom(value);
            UUID creaUUID = UUID.fromString(orderCreatedEvent.getOrderUUID());

//            orderService.createOrder();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

    }

    private void cancelOrderEvent(byte[] value) {
        try {
            OrderCancelledEvent cancelledEvent = OrderCancelledEvent.parseFrom(value);
            UUID cancelUUID = UUID.fromString(cancelledEvent.getOrderUuid());

            orderService.cancelOrder(cancelUUID);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

}
