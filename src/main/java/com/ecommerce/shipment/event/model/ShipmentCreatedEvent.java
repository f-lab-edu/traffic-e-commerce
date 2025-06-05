package com.ecommerce.shipment.event.model;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ShipmentCreatedEvent {

    private UUID orderUUID;

    private UUID deliveryUUID;

    private ExternalShippingStatus status;

    private LocalDateTime estmtDeliverDt;

    public static ShipmentCreatedEvent of(Shipment shipment) {
       return ShipmentCreatedEvent.builder()
                .orderUUID(shipment.getOrderUUID())
                .deliveryUUID(shipment.getShipUUID())
                .status(shipment.getStatus())
                .estmtDeliverDt(shipment.getEstmtDeliverDt())
                .build();
    }

}
