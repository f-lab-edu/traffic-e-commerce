package com.ecommerce.shipment.event.model;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ShipmentStatusChangedEvent {

    private UUID orderUUID;

    private UUID shipUUID;

    private ExternalShippingStatus status;

    private String carrierName;

    private String trackingNumber;

    private LocalDateTime changedAt;

    public static ShipmentStatusChangedEvent of(Shipment shipment) {
        return ShipmentStatusChangedEvent.builder()
                .orderUUID(shipment.getOrderUUID())
                .shipUUID(shipment.getShipUUID())
                .status(shipment.getStatus())
                .carrierName(shipment.getCarrierName())
                .trackingNumber(shipment.getTrackingNumber())
                .build();

    }
}
