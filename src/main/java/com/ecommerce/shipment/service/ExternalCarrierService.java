package com.ecommerce.shipment.service;

import com.ecommerce.shipment.domain.Shipment;
import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.dto.request.CarrierUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCarrierService {

    private final ShipmentService shipmentService;
    private final Random random = new Random();

    private static final String[] CARRIER_NAMES = {
            "Express Delivery", "Fast Shipping", "Quick Carrier", "Safe Transport"
    };

    @Async
    public void requestDelivery(Shipment shipment) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Requesting delivery to external carrier for Shipment : {}", shipment.getShipUUID());

                // 택배사 정보 생성 (랜덤)
                String carrierName = CARRIER_NAMES[random.nextInt(CARRIER_NAMES.length)];
                String trackingNumber = generateTrackingNumber();

                // 배송 접수 처리 시간 시뮬레이션 (1-3초)
                TimeUnit.SECONDS.sleep(1 + random.nextInt(3));

                CarrierUpdateRequest readyRequest = CarrierUpdateRequest.of(
                        carrierName,
                        trackingNumber,
                        ExternalShippingStatus.READY_FOR_PICKUP
                );


                // 한 번의 트랜잭션으로 택배사 정보 + 상태 변경
                shipmentService.updateCarrierInfoAndStatus(
                        shipment.getShipUUID(),
                        readyRequest
                );

                log.info("Delivery registered with carrier: {}, tracking: {}", carrierName, trackingNumber);

                // 배송 상태 변경 시뮬레이션 시작
                simulateDeliveryProcess(shipment.getShipUUID());

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
                TimeUnit.SECONDS.sleep(5 + random.nextInt(10));
                shipmentService.updateShipmentStatusByUUID(shipUUID, ExternalShippingStatus.SHIPPING);

                // 최종 배송 완료/실패 (90% 성공률)
                TimeUnit.SECONDS.sleep(5 + random.nextInt(10));
                if (random.nextInt(10) < 9) {
                    shipmentService.updateShipmentStatusByUUID(shipUUID, ExternalShippingStatus.DELIVERED);
                    log.info("Delivery completed successfully: {}", shipUUID);
                } else {
                    shipmentService.updateShipmentStatusByUUID(shipUUID, ExternalShippingStatus.FAILED);
                    log.info("Delivery failed: {}", shipUUID);
                }

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
            sb.append((char) ('A' + random.nextInt(26)));
        }

        // 10자리 숫자
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
