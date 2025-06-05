package com.ecommerce.shipment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "order_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID orderUUID;

    @Column(name = "ship_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID shipUUID;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExternalShippingStatus status;

    @Column(name = "estmt_deliver_dt")
    private LocalDateTime estmtDeliverDt;

    @Column(name = "crea_dt")
    private LocalDateTime creaDt;

    @Column(name = "updt_dt")
    private LocalDateTime updtDt;

    @PrePersist
    protected void onCreate() {
        creaDt = LocalDateTime.now();
        updtDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updtDt = LocalDateTime.now();
    }

    // 택배사 정보 업데이트
    public void updateCarrierDetailInfo(String carrierName, String trackingNumber) {
        Shipment.builder()
                .id(id)
                .orderUUID(orderUUID)
                .shipUUID(shipUUID)
                .carrierName(carrierName)
                .trackingNumber(trackingNumber)
                .status(status)
                .estmtDeliverDt(estmtDeliverDt)
                .creaDt(creaDt)
                .build();
    }

    // 상태 업데이트 메서드 (불변성 유지)
    public Shipment updateShippingStatus(ExternalShippingStatus newStatus) {
        return Shipment.builder()
                .id(id)
                .orderUUID(orderUUID)
                .shipUUID(shipUUID)
                .carrierName(carrierName)
                .trackingNumber(trackingNumber)
                .status(newStatus)
                .estmtDeliverDt(estmtDeliverDt)
                .creaDt(creaDt)
                .build();
    }

    public boolean hasStatusDelivered() {
        return status.equals(ExternalShippingStatus.DELIVERED);
    }

    public boolean hasStatusFailed() {
        return status.equals(ExternalShippingStatus.FAILED);
    }


    public boolean hasDeliveryDetailInfo() {
        return hasCarrierName() && hasTrackingNumber();
    }

    public boolean hasCarrierName() {
        return carrierName != null;
    }


    public boolean hasTrackingNumber() {
        return trackingNumber != null;
    }

}
