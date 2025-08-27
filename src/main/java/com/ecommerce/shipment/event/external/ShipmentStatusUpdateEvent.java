package com.ecommerce.shipment.event.external;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ShipmentStatusUpdateEvent {

    private final UUID shipUUID;

    private final ExternalShippingStatus status;

    public static ShipmentStatusUpdateEvent of(UUID shipUUID, ExternalShippingStatus status) {
        return ShipmentStatusUpdateEvent.builder()
                .shipUUID(shipUUID).status(status)
                .build();
    }
}
