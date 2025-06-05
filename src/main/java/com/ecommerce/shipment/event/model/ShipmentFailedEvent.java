package com.ecommerce.shipment.event.model;


import com.ecommerce.shipment.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentFailedEvent {

    private UUID orderUUID;

    private UUID shipUUID;

    private LocalDateTime failedDt;


    public static ShipmentFailedEvent of(Shipment shipment) {
        return ShipmentFailedEvent.builder()
                .orderUUID(shipment.getOrderUUID()).shipUUID(shipment.getShipUUID()).failedDt(LocalDateTime.now())
                .build();
    }


}
