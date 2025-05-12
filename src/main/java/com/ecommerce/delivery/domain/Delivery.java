package com.ecommerce.delivery.domain;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="deliveries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(name = "order_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID orderUUID;

    @Column(name = "delivery_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID deliveryUUID;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(name = "estmt_deliver_dt")
    private LocalDateTime estmtDeliverDt;

    @Column(name = "crea_dt")
    private LocalDateTime creaDt;

    @Column(name = "updt_dt")
    private LocalDateTime updtDt;

    @PrePersist
    protected void onCreate() {
        this.creaDt = LocalDateTime.now();
        this.updtDt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updtDt = LocalDateTime.now();
    }

}
