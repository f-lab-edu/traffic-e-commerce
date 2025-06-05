package com.ecommerce.shipment.event.consumer;

import com.ecommerce.proto.EdaMessage;
import com.ecommerce.shipment.service.ShipmentService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentEventConsumer {

    private final ShipmentService shipmentService;


    @KafkaListener(topics = "shipment-events", groupId = "shipment-service")
    public void consumeShipmentEvents(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] value = record.value();
            log.info("Shipment consume: order key={}", key);
            if ("order.succeed".equals(key)) {
                operateOrderSucceed(value);
            } else if ("order.cancelled".equals(key)) {
                operateOrderCancelled(value);
            }

        } catch (Exception e) {
            log.error("[shipment-consumer] : order error {}", e.getMessage());
        }
    }

    private void operateOrderSucceed(byte[] eventBytes) {
        try {
            EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(eventBytes);
            UUID orderUUID = UUID.fromString(event.getOrderUUID());
            shipmentService.getShipListByOrderUUID(List.of(orderUUID));
        } catch (InvalidProtocolBufferException e) {
            log.error("[shipment-operate] : ProtoBuf parse error : {}", e.getMessage());
        } catch (Exception e) {
            log.error("[shipment-operate] : created Order error : {}", e.getMessage());
        }


    }

    private void operateOrderCancelled(byte[] eventBytes) {
        try {
            EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(eventBytes);
            UUID orderUUID = UUID.fromString(event.getOrderUUID());
//            shipmentService
        } catch (InvalidProtocolBufferException e) {
            log.error("ProtoBuf parse error : {}", e.getMessage());
        } catch (Exception e) {
            log.error("Canceled Order  error : {}", e.getMessage());
        }


    }

}
