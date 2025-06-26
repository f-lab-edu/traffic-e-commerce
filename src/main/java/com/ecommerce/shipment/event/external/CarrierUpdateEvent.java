package com.ecommerce.shipment.event.external;

import com.ecommerce.shipment.dto.request.CarrierUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CarrierUpdateEvent {

    private final UUID shipUUID;

    private final CarrierUpdateRequest request;

    public static CarrierUpdateEvent of(UUID shipUUID, CarrierUpdateRequest request) {
        return CarrierUpdateEvent.builder()
                .shipUUID(shipUUID).request(request)
                .build();
    }
}
