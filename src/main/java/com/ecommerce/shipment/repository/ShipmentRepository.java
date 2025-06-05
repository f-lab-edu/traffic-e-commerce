package com.ecommerce.shipment.repository;

import com.ecommerce.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 주문 UUID로 배송 조회
    Optional<Shipment> findByOrderUUID(UUID id);

    // 배송 UUID로 배송 조회
    Optional<Shipment> findByShipUUID(UUID shipUUID);

    // 여러 주문의 배송 조회 목록 조회 (유저의 전체 배송)
    List<Shipment> findByOrderUUIDin(List<UUID> orderUUIDs);

}
