package com.ecommerce.delivery.service;

import com.ecommerce.delivery.domain.Delivery;
import com.ecommerce.delivery.domain.DeliveryStatus;
import com.ecommerce.delivery.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public List<Delivery> getDeliveryList(UUID orderUUID) {

        return deliveryRepository.findByDeliveryUUID(orderUUID);



    }


    @Transactional
    public void updateDeliveryStatus(UUID deliveryUUID, DeliveryStatus status,
                                     String carrierName, String trackingNumber) {
        Delivery delivery = deliveryRepository.findByDeliveryUUID(deliveryUUID)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryUUID));

        // 배송 정보 업데이트 로직
        delivery = Delivery.builder()
                .id(delivery.getId())
                .orderUUID(delivery.getOrderUUID())
                .deliveryUUID(delivery.getDeliveryUUID())
                .carrierName(carrierName)
                .trackingNumber(trackingNumber)
                .status(status)
                .estmtDeliverDt(delivery.getEstmtDeliverDt())
                .creaDt(delivery.getCreaDt())
                .build();

        deliveryRepository.save(delivery);

        // 배송 상태 변경 이벤트 발행
//        eventPublisher.publishDeliveryStatusChanged(delivery);
    }

    // 이벤트 상태 추적 클래스
    private static class OrderEventStatus {
        private boolean paymentSuccess = false;
        private boolean inventoryConfirmed = false;

        public boolean isPaymentSuccess() { return paymentSuccess; }
        public void setPaymentSuccess(boolean value) { this.paymentSuccess = value; }

        public boolean isInventoryConfirmed() { return inventoryConfirmed; }
        public void setInventoryConfirmed(boolean value) { this.inventoryConfirmed = value; }
    }
}
