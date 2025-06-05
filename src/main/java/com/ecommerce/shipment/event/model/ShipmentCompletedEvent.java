package com.ecommerce.shipment.event.model;

import com.ecommerce.shipment.domain.Shipment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ShipmentCompletedEvent {

    private UUID orderUUID;

    private UUID shipUUID;

    private String trackingNumber;

    private LocalDateTime completedDt;

    public static ShipmentCompletedEvent of(Shipment shipment) {
        return ShipmentCompletedEvent.builder()
                .orderUUID(shipment.getOrderUUID())
                .shipUUID(shipment.getShipUUID())
                .trackingNumber(shipment.getTrackingNumber())
                .completedDt(LocalDateTime.now())
                .build();
    }

}
