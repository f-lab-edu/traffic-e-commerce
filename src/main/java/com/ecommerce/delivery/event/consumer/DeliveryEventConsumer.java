package com.ecommerce.delivery.event.consumer;

import com.ecommerce.delivery.domain.Delivery;
import com.ecommerce.delivery.service.DeliveryService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final DeliveryService deliveryService;


    @KafkaListener(topics = "delivery-events", groupId = "delivery-service")
    public void consumeDeliveryEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();
            log.info("Delivery consume: order key={}", key);
            if ("order.created".equals(key)) {
                List<Delivery> ddd = deliveryService.getDeliveryList(new UUID("ddd"));


//                operateOrderCreated(value);
            } else if ("order.cancelled".equals(key)) {
//                operateOrderCancelled(value);
            }

        } catch (Exception e) {
            log.error("[delivery-consumer] :  consume order event {}", e.getMessage());
        }
    }


    private void operateOrderCreated(byte[] eventBytes) {
        try {
            EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(eventBytes);

            UUID orderUUID = UUID.fromString(event.getOrderUUID());
            deliveryService.getDeliveryList(orderUUID);
        } catch (InvalidProtocolBufferException e) {
            log.error("[delivery-operate] : ProtoBuf parse error : {}", e.getMessage());
        } catch (Exception e) {
            log.error("[delivery-operate] : created Order error : {}", e.getMessage());
        }


    }



}
