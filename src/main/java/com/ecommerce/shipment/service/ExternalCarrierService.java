package com.ecommerce.shipment.service;

import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.dto.request.CarrierUpdateRequest;
import com.ecommerce.shipment.event.external.CarrierUpdateEvent;
import com.ecommerce.shipment.event.external.ShipmentStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCarrierService {

    @SuppressWarnings("java:S2245")
    private final SecureRandom secureRandom = new SecureRandom();

    private final ApplicationEventPublisher eventPublisher;

    private static final int DELIVERY_SUCCESS_RATE = 9; // 90% 성공률
    private static final int RANDOM_RANGE = 10;

    private static final String[] CARRIER_NAMES = {
            "Express Delivery", "Fast Shipping", "Quick Carrier", "Safe Transport"
    };

    @Async
    public void requestDelivery(Shipment shipment) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Requesting delivery to external carrier for Shipment : {}", shipment.getShipUUID());

                // 택배사 정보 생성 (랜덤)
                String carrierName = CARRIER_NAMES[secureRandom.nextInt(CARRIER_NAMES.length)];
                String trackingNumber = generateTrackingNumber();

                // 배송 접수 처리 시간 시뮬레이션 (1-3초)
                TimeUnit.SECONDS.sleep(1 + secureRandom.nextInt(3));

                CarrierUpdateRequest readyRequest = CarrierUpdateRequest.of(
                        carrierName,
                        trackingNumber,
                        ExternalShippingStatus.READY_FOR_PICKUP
                );

                log.info("Delivery registered with carrier: {}, tracking: {}", carrierName, trackingNumber);

                // 배송 상태 변경 시뮬레이션 시작
                simulateDeliveryProcess(shipment.getShipUUID());
                eventPublisher.publishEvent(CarrierUpdateEvent.of(shipment.getShipUUID(), readyRequest));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Delivery request interrupted", e);
            } catch (Exception e) {
                log.error("Error processing delivery request", e);
            }
        });
    }

    private void simulateDeliveryProcess(UUID shipUUID) {
        CompletableFuture.runAsync(() -> {
            try {
                // 배송출발 단계
                TimeUnit.SECONDS.sleep(5 + secureRandom.nextInt(10));
                eventPublisher.publishEvent(ShipmentStatusUpdateEvent.of(shipUUID, ExternalShippingStatus.SHIPPING));

                // 최종 배송 완료/실패 (90% 성공률)
                ExternalShippingStatus finalStatus = decideShippingStatus();
                eventPublisher.publishEvent(ShipmentStatusUpdateEvent.of(shipUUID, finalStatus));
                log.info("Delivery completed : {} status : {}", shipUUID, finalStatus.toString());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Delivery simulation interrupted", e);
            } catch (Exception e) {
                log.error("Error in delivery simulation", e);
            }
        });
    }

    private String generateTrackingNumber() {
        // 추적 번호 형식: 2자리 알파벳 + 10자리 숫자
        StringBuilder sb = new StringBuilder();

        // 2자리 대문자 알파벳
        for (int i = 0; i < 2; i++) {
            sb.append((char) ('A' + secureRandom.nextInt(26)));
        }

        // 10자리 숫자
        for (int i = 0; i < 10; i++) {
            sb.append(secureRandom.nextInt(10));
        }

        return sb.toString();
    }

    private ExternalShippingStatus decideShippingStatus() {
        return secureRandom.nextInt(RANDOM_RANGE) < DELIVERY_SUCCESS_RATE ?
                ExternalShippingStatus.DELIVERED : ExternalShippingStatus.FAILED;
    }

}
