package com.ecommerce.shipment.event.producer;

import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.event.model.ShipmentCompletedEvent;
import com.ecommerce.shipment.event.model.ShipmentCreatedEvent;
import com.ecommerce.shipment.event.model.ShipmentFailedEvent;
import com.ecommerce.shipment.event.model.ShipmentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 배송 생성 이벤트 발행
     */
    public void publishShipCreated(Shipment shipment) {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.of(shipment);

        log.info("Publishing shipment created event: {}", event);
        kafkaTemplate.send("shipment", "created", event);
    }

    /**
     * 배송 실패 이벤트 발행 (주문 도메인)
     */
    public void publishShipFailed(Shipment shipment) {
        ShipmentFailedEvent event = ShipmentFailedEvent.of(shipment);

        log.info("Publishing shipment failed event: {}", event);
        kafkaTemplate.send("shipment", "failed", event);
    }

    /**
     * 배송 상태 변경 이벤트 발행 (내부 확인용)
     */
    public void publishShipStatusChanged(Shipment shipment) {
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.of(shipment);
        log.info("Publishing shipment status changed event: {}", event);
        kafkaTemplate.send("shipment", "changed", event);
    }

    /**
     * 배송 완료 이벤트 발행 (주문 도메인)
     */
    public void publishShipCompleted(Shipment shipment) {
        ShipmentCompletedEvent event = ShipmentCompletedEvent.of(shipment);
        log.info("Publishing delivery.completed event: {}", event);
        kafkaTemplate.send("shipment", "completed", event);
    }
}
