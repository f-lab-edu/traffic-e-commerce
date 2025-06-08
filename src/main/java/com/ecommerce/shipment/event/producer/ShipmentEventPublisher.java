package com.ecommerce.shipment.event.producer;

import com.ecommerce.proto.ShipmentCompletedEvent;
import com.ecommerce.proto.ShipmentCreatedEvent;
import com.ecommerce.proto.ShipmentFailedEvent;
import com.ecommerce.proto.ShipmentStatusChangedEvent;
import com.ecommerce.shipment.domain.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 배송 생성 이벤트 발행 (택배사)
    public void publishShipCreated(Shipment shipment) {
        ShipmentCreatedEvent shipCreateEvent = ShipmentCreatedEvent.newBuilder()
                .setOrderUuid(shipment.getOrderUUID().toString())
                .setShipUuid(shipment.getShipUUID().toString())
                .setStatus(shipment.getStatus().toKafkaString())
                .setEstmtDeliverDt(shipment.getEstmtDeliverDt().toInstant(ZoneOffset.UTC).toEpochMilli()).build();

        log.info("Publishing shipment created event: {}", shipCreateEvent);
        kafkaTemplate.send("shipment", "created", shipCreateEvent.toByteArray());
    }

    // 배송 실패 이벤트 발행 (주문 도메인)
    public void publishShipFailed(Shipment shipment) {
        ShipmentFailedEvent shipmentFailedEvent = ShipmentFailedEvent.newBuilder()
                .setOrderUuid(shipment.getOrderUUID().toString())
                .setShipUuid(shipment.getShipUUID().toString())
                .setFailedDt(shipment.getUpdtDt().toInstant(ZoneOffset.UTC).toEpochMilli()).build();

        log.info("Publishing shipment failed event: {}", shipmentFailedEvent);
        kafkaTemplate.send("shipment", "failed", shipmentFailedEvent.toByteArray());
    }

    // 배송 상태 변경 이벤트 발행 (내부 확인용)
    public void publishShipStatusChanged(Shipment shipment) {
        ShipmentStatusChangedEvent changedEvent = ShipmentStatusChangedEvent.newBuilder()
                .setOrderUuid(shipment.getOrderUUID().toString())
                .setShipUuid(shipment.getShipUUID().toString())
                .setStatus(shipment.getStatus().toKafkaString())
                .setCarrierName(shipment.getCarrierName())
                .setTrackingNumber(shipment.getTrackingNumber())
                .setChangedAt(shipment.getUpdtDt().toInstant(ZoneOffset.UTC).toEpochMilli()).build();

        log.info("Publishing shipment status changed event: {}", changedEvent);
        kafkaTemplate.send("shipment", "changed", changedEvent.toByteArray());
    }

    // 배송 완료 이벤트 발행 (주문 도메인)
    public void publishShipCompleted(Shipment shipment) {
        ShipmentCompletedEvent shipComplete = ShipmentCompletedEvent.newBuilder().setOrderUuid(shipment.getOrderUUID().toString())
                .setShipUuid(shipment.getShipUUID().toString())
                .setTrackingNumber(shipment.getTrackingNumber())
                .setCompletedDt(shipment.getUpdtDt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();

        log.info("Publishing delivery.completed event: {}", shipComplete);
        kafkaTemplate.send("shipment", "completed", shipComplete.toByteArray());
    }
}
