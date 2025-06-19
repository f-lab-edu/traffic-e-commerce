package com.ecommerce.shipment.domain;

import com.ecommerce.shipment.event.producer.ShipmentEventPublisher;
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

    // 택배사 정보 업데이트 - JPA Dirty Checking 활용
    public void updateCarrierDetailInfo(String carrierName, String trackingNumber) {
        this.carrierName = carrierName;
        this.trackingNumber = trackingNumber;
        // @PreUpdate에 의해 updtDt 자동 갱신
    }

    // 상태 업데이트 메서드 - JPA Dirty Checking 활용
    public void updateShippingStatus(ExternalShippingStatus newStatus) {
        // 1. 비즈니스 규칙 검증
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(String.format("Cannot transition from %s to %s", status, newStatus));
        }

        // 2. 상태 변경 (Dirty Checking 으로 자동 UPDATE)
        this.status = newStatus;
        // 3. @PreUpdate에 의해 updtDt 자동 갱신
    }

    public boolean hasStatusDelivered() {
        return status.equals(ExternalShippingStatus.DELIVERED);
    }

    // 비즈니스 로직 처리 (함수형 enum 활용)
    public void processStatusChange(ShipmentEventPublisher publisher) {
        status.process(this, publisher);
    }

}
