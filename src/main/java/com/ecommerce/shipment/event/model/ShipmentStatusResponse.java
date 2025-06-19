package com.ecommerce.shipment.event.model;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.ecommerce.payment.paymentDomain.PaymentStatus.PENDING;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusResponse {

    private Long orderId;
    private UUID shipUUID;
    private ExternalShippingStatus status;
    private String carrierName;
    private String trackingNumber;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 진행 상태 설명
    private String statusDescription;

    public static ShipmentStatusResponse from(Shipment shipment) {
        return ShipmentStatusResponse.builder()
                .shipUUID(shipment.getShipUUID())
                .status(shipment.getStatus())
                .carrierName(shipment.getCarrierName())
                .trackingNumber(shipment.getTrackingNumber())
                .estimatedDeliveryDate(shipment.getEstmtDeliverDt())
                .createdAt(shipment.getCreaDt())
                .updatedAt(shipment.getUpdtDt())
                .statusDescription(shipment.getStatus().toString())
                .build();
    }
}
