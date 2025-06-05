package com.ecommerce.shipment.service;

import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.domain.ExternalShippingStatus;
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
import static com.ecommerce.shipment.domain.ExternalShippingStatus.DELIVERED;

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

            // 배송 저장
            shipmentRepository.save(preparedShip);
            log.info("Shipment created: {}", shipment.getShipUUID());

            // 외부 택배사에 비동기 요청
            externalCarrierService.requestDelivery(shipment);

            // 배송 생성 이벤트 발행
            eventPublisher.publishShipCreated(shipment);

        } catch (Exception e) {
            log.info("Failed to execute shipment for order: {} {}", shipment.getOrderUUID(), e.getMessage());
        }

    }

    /**
     * 배송 취소 처리
     */
    @Transactional
    public void cancelShipment(Shipment shipment) {
        try {
            Optional<Shipment> optionalShip = shipmentRepository.findByOrderUUID(shipment.getOrderUUID());

            // 배송 진행 전의 취소 방지
            if (optionalShip.isEmpty()) {
                log.warn("No shipment found to cancel for order: {}", shipment.getOrderUUID());
                return;
            }

            Shipment progressedShip = optionalShip.get();

            // 배송 중이거나 완료된 경우 취소 안됨
            if (progressedShip.hasStatusDelivered()) {
                log.warn("Cannot cancel delivered shipment for order: {}", progressedShip.getOrderUUID());
                return;
            }

            // 배송 취소 상태로 변경 > soft delete
            Shipment cancelledShip = shipment.updateShippingStatus(CANCELLED);
            shipmentRepository.save(cancelledShip);

            log.info("Shipment cancelled for order: {}", cancelledShip.getOrderUUID());


        } catch (Exception e) {
            log.info("Failed to cancel shipment for order: {} : {}", shipment.getOrderUUID(), e.getMessage());
        }
    }

    @Transactional
    public void updateShipmentStatus(Shipment shipment) {

        try {

            Shipment selectedShip = shipmentRepository.findByShipUUID(shipment.getShipUUID())
                    .orElseThrow(() -> new EntityNotFoundException("Shipment not found: " + shipment.getShipUUID()));


            // 택배사 정보 / 송장 정보 업데이트
            if (selectedShip.hasDeliveryDetailInfo()) {
                selectedShip.updateCarrierDetailInfo(selectedShip.getCarrierName(), selectedShip.getTrackingNumber());
            }
            selectedShip.updateShippingStatus(DELIVERED);

            shipmentRepository.save(selectedShip);

            log.info("Shipment status updated: {} -> {}", shipment.getShipUUID(), shipment.getStatus());

            // 배송 완료/실패 이벤트 발행
            if (shipment.hasStatusDelivered()) {
                eventPublisher.publishShipCompleted(shipment);
            } else if (shipment.hasStatusFailed()) {
                eventPublisher.publishShipFailed(shipment);
            }

            // 상태 변경 이벤트 발행
            eventPublisher.publishShipStatusChanged(shipment);

        }catch (Exception e) {
            log.info("Failed to update shipment for order: {} : {}", shipment.getOrderUUID(), e.getMessage());
        }



    }

    public List<Shipment> getShipListByOrderUUID(List<UUID> orderUUIDs) {
        return shipmentRepository.findByOrderUUIDin(orderUUIDs);
    }


}
