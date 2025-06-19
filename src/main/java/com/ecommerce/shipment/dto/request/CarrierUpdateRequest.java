package com.ecommerce.shipment.dto.request;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarrierUpdateRequest {

    private final String carrierName;
    private final String trackingNumber;
    private final ExternalShippingStatus newStatus;

    public static CarrierUpdateRequest of(String carrierName, String trackingNumber, ExternalShippingStatus status) {
        return CarrierUpdateRequest.builder()
                .carrierName(carrierName)
                .trackingNumber(trackingNumber)
                .newStatus(status)
                .build();
    }

}
