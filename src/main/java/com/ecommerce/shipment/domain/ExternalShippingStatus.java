package com.ecommerce.shipment.domain;


import com.ecommerce.shipment.event.producer.ShipmentEventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
public enum ExternalShippingStatus {


    READY((shipment, publisher) -> publisher.publishShipStatusChanged(shipment)),

    READY_FOR_PICKUP((shipment, publisher) -> publisher.publishShipStatusChanged(shipment)),

    SHIPPING((shipment, publisher) -> publisher.publishShipStatusChanged(shipment)),

    DELIVERED((shipment, publisher) -> publisher.publishShipCompleted(shipment)),

    FAILED((shipment, publisher) -> publisher.publishShipFailed(shipment)),

    CANCELLED((shipment, publisher) -> publisher.publishShipCompleted(shipment));

    // 함수형 인터페이스 - 각 배송 상태별 처리 로직
    private final BiConsumer<Shipment, ShipmentEventPublisher> processor;

    ExternalShippingStatus(BiConsumer<Shipment, ShipmentEventPublisher> processor) {
        this.processor = processor;
    }

    // 상태별 비즈니스 로직 실행
    public void process(Shipment shipment, ShipmentEventPublisher publisher) {
        this.processor.accept(shipment, publisher);
    }


    // 카프카 발행용 String 변환 - DELIVERED, FAILED 등 문자열 그대로 사용
    public String toKafkaString() {
        return this.name();
    }

    // 카프카에서 받은 String을 Enum으로 변환
    public static ExternalShippingStatus fromKafkaString(String status) {
        try {
            return ExternalShippingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown shipping status: " + status, e);
        }
    }

    /**
     * 상태 전환 가능 여부 (비즈니스 규칙)
     */
    public boolean canTransitionTo(ExternalShippingStatus newStatus) {
        return switch (this) {
            case READY -> newStatus == READY_FOR_PICKUP || newStatus == CANCELLED;
            case READY_FOR_PICKUP -> newStatus == SHIPPING || newStatus == CANCELLED;
            case SHIPPING -> newStatus == DELIVERED || newStatus == FAILED;
            case DELIVERED, FAILED, CANCELLED -> false; // 최종 상태
        };
    }

    /**
     * 최종 상태 여부
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == FAILED || this == CANCELLED;
    }

}
