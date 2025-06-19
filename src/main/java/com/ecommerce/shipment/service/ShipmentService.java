package com.ecommerce.shipment.service;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.dto.request.CarrierUpdateRequest;
import com.ecommerce.shipment.event.producer.ShipmentEventPublisher;
import com.ecommerce.shipment.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ecommerce.shipment.domain.ExternalShippingStatus.CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventPublisher eventPublisher;
    private final ExternalCarrierService externalCarrierService;

    /**
     * 배송 생성 및 시작
     * 자동으로 택배사에 배송요청 이벤트 발행
     */
    @Transactional
    public void createExecuteShipment(Shipment shipment) {
        try {
            // 중복 배송 생성 방지
            Optional<Shipment> existedShip = shipmentRepository.findByOrderUUID(shipment.getOrderUUID());
            if (existedShip.isPresent()) {
                log.warn("Shipment already exists for order: {}", shipment.getOrderUUID());
            }

            // 배송 엔티티 생성
            Shipment preparedShip = Shipment.builder()
                    .orderUUID(shipment.getOrderUUID())
                    .shipUUID(UUID.randomUUID())
                    .status(ExternalShippingStatus.READY)
                    .estmtDeliverDt(LocalDateTime.now().plusDays(3)) // 예상 3일로 고정
                    .build();


            shipmentRepository.save(preparedShip); // 배송 저장
            eventPublisher.publishShipCreated(shipment); // 배송 생성 이벤트 발행
            log.info("Shipment created: {}", shipment.getShipUUID());

            // 외부 택배사에 비동기 요청
            externalCarrierService.requestDelivery(shipment);
        } catch (Exception e) {
            log.info("Failed to execute shipment for order: {} {}", shipment.getOrderUUID(), e.getMessage());
        }
    }


    //  (엔티티 재사용)
    @Transactional
    public void updateCarrierInfoAndStatus(UUID shipUUID, CarrierUpdateRequest readyRequest) {
        // 한 번만 DB 조회
        Shipment shipment = shipmentRepository.findByShipUUID(shipUUID)
                .orElseThrow(() -> new EntityNotFoundException("No shipment found for order: " + shipUUID));

        String carrierName = readyRequest.getCarrierName();
        String trackingNumber = readyRequest.getTrackingNumber();
        ExternalShippingStatus newStatus = readyRequest.getNewStatus();

        // 엔티티 재사용
        updateCarrierInfo(shipment, carrierName, trackingNumber);
        updateShipmentStatus(shipment, newStatus);
    }

    @Transactional
    public void updateShipmentStatusByUUID(UUID shipUUID, ExternalShippingStatus newStatus) {
        Shipment shipment = shipmentRepository.findByShipUUID(shipUUID)
                .orElseThrow(() -> new EntityNotFoundException("No shipment found for order: " + shipUUID));
        updateShipmentStatus(shipment, newStatus);
    }

    // 엔티티 기반 처리 (내부 로직에서 연속 작업)
    private void updateCarrierInfo(Shipment shipment, String carrierName, String trackingNumber) {
        shipment.updateCarrierDetailInfo(carrierName, trackingNumber);
        log.info("Update external carrier {} - {} {}", shipment.getShipUUID(), carrierName, trackingNumber);
    }

    private void updateShipmentStatus(Shipment shipment, ExternalShippingStatus newStatus) {
        shipment.updateShippingStatus(newStatus);
        shipment.processStatusChange(eventPublisher);
        log.info("배송 상태 변경: {} -> {}", shipment.getShipUUID(), newStatus);
    }


    /**
     * 배송 취소 처리
     */
    @Transactional
    public void cancelShipment(Shipment shipment) {
        try {
            Shipment optionalShip = shipmentRepository.findByOrderUUID(shipment.getOrderUUID())
                    .orElseThrow(() -> new EntityNotFoundException("No shipment found for order: " + shipment.getOrderUUID()));

            // 배송 진행 전의 취소 방지
            if (optionalShip.hasStatusDelivered()) {
                throw new IllegalStateException("Cannot cancel delivered shipment for order: " + shipment.getOrderUUID());
            }

            shipment.updateShippingStatus(CANCELLED); // JPA Dirty Checking 활용
            shipment.processStatusChange(eventPublisher); // 상태별 이벤트 처리
            log.info("Shipment cancelled for order: {}", shipment.getOrderUUID());

        } catch (Exception e) {
            log.info("Failed to cancel shipment for order: {} : {}", shipment.getOrderUUID(), e.getMessage());
        }
    }


    public Shipment getShipmentByOrderUUID(UUID orderUUIDs) {
        return shipmentRepository.findByOrderUUID(orderUUIDs)
                .orElseThrow(() -> new EntityNotFoundException("No shipment found for order: " + orderUUIDs.toString()));
    }

    public List<Shipment> getShipListByOrderUUID(List<UUID> orderUUIDs) {
        return shipmentRepository.findByOrderUUIDin(orderUUIDs);
    }


}
