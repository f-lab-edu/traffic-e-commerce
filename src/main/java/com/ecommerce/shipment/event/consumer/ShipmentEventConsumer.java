package com.ecommerce.shipment.event.consumer;

import com.ecommerce.shipment.domain.OrderLifecycleMessage;
import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.service.ShipmentService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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

            if ("shipment.requested".equals(key)) {
                operateShipRequested(value);
            } else if ("shipment.cancelled".equals(key)) {
                operateShipCancelled(value);
            }

        } catch (Exception e) {
            log.error("[shipment-consumer] : order error {}", e.getMessage());
        }
    }

    private void operateShipRequested(byte[] eventBytes) {
        try {
            UUID orderUUID = parseOrderUUID(eventBytes);
            Shipment succeedOrder = shipmentService.getShipmentByOrderUUID(orderUUID);
            // Shipment protocol buffer로 변환한뒤 로직실행
            shipmentService.createShipExecution(succeedOrder);
        } catch (Exception e) {
            log.error("[shipment-consumer] : request ship error : {}", e.getMessage());
        }
    }

    private void operateShipCancelled(byte[] eventBytes) {
        try {
            UUID orderUUID = parseOrderUUID(eventBytes);
            Shipment updatedShip = shipmentService.getShipmentByOrderUUID(orderUUID);
            shipmentService.cancelShipment(updatedShip);
        } catch (Exception e) {
            log.error("[shipment-consumer] : cancel ship  error : {}", e.getMessage());
        }
    }

    private UUID parseOrderUUID(byte[] eventBytes) {
        try {
            OrderLifecycleMessage.OrderLifecycleEvent orderEvent = OrderLifecycleMessage.OrderLifecycleEvent.parseFrom(eventBytes);
            return UUID.fromString(orderEvent.getOrderUuid());
        } catch (InvalidProtocolBufferException e) {
            log.error("ProtoBuf parse error : {}", e.getMessage());
            throw new RuntimeException("Failed to parse order UUID", e);
        }
    }

}
