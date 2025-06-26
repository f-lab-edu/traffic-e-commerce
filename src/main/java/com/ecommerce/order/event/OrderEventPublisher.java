package com.ecommerce.order.event;

import com.ecommerce.order.orderEntity.OrderItem;
import com.ecommerce.proto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    // 이벤트 발행 - 재고 차감 요청
    public void publishInventoryDeductionRequested(UUID orderUUID, List<OrderItem> orderItems) {
        try {

            StockDeductEvent.Builder eventBuilder = StockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setDeductDt(System.currentTimeMillis());

            // 주문 아이템들을 재고 차감 아이템으로 변환
            for (OrderItem item : orderItems) {
                eventBuilder.addItems(DeductItems.newBuilder()
                        .setProductUuid(item.getProductUUID().toString())
                        .setDeductQuantity(item.getQuantity())
                        .setRemainingStock(0) // 요청 시에는 0으로 설정
                        .build());
            }

            StockDeductEvent event = eventBuilder.build();
            kafkaTemplate.send("order-event", "stock.deduction.request", event.toByteArray());

            log.info("Publish stock deduction : orderUUID={}, itemCount={}", orderUUID, orderItems.size());

        } catch (Exception e) {
            log.error("Fail to stock deduction: orderUUID={}, error={}",
                    orderUUID, e.getMessage());
            throw new RuntimeException("재고 차감 요청 이벤트 발행 실패", e);
        }
    }

    // 이벤트 발행- 재고 복구 요청
    public void publishInventoryRestorationRequested(UUID orderUUID, List<OrderItem> orderItems, String reason) {
        try {
            StockRestoredEvent.Builder eventBuilder = StockRestoredEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setRestoredDt(System.currentTimeMillis());

            for (OrderItem item : orderItems) {
                eventBuilder.addItems(RestoredItems.newBuilder()
                        .setProductUuid(item.getProductUUID().toString())
                        .setRestoredQuantity(item.getQuantity())
                        .setCurrentStock(0) // 요청 시에는 0으로 설정
                        .build());
            }

            StockRestoredEvent event = eventBuilder.build();

            kafkaTemplate.send("order-event", "stock.restore.request", event.toByteArray());

            log.info("재고 복구 요청 이벤트 발행: orderUUID={}, reason={}, itemCount={}", orderUUID, reason, orderItems.size());

        } catch (Exception e) {
            log.error("재고 복구 요청 이벤트 발행 실패: orderUUID={}, error={}", orderUUID, e.getMessage());
            throw new RuntimeException("재고 복구 요청 이벤트 발행 실패", e);
        }
    }

    /**
     * 결제 요청 이벤트 발행 (JSON 형태)
     */
    public void publishPaymentRequested(UUID orderUUID, UUID userId, BigDecimal totalAmount,
                                        String deliveryAddress, String contactPhone) {
        try {
            PaymentMessage.PaymentRequestedEvent paymentRequest = PaymentMessage.PaymentRequestedEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setAmount(totalAmount.doubleValue())
                    .setRequestedDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("payment-events", "payment.requested", paymentRequest.toByteArray());

            log.info("결제 요청 이벤트 발행: orderUUID={}, amount={}", orderUUID, totalAmount);

        } catch (Exception e) {
            log.error("결제 요청 이벤트 발행 실패: orderUUID={}, error={}", orderUUID, e.getMessage());
            throw new RuntimeException("결제 요청 이벤트 발행 실패", e);
        }
    }


    // 이벤트 발행 - 배송 요청
    public void publishShipmentRequested(UUID orderUUID, UUID userId, String deliveryAddress, String contactPhone) {
        try {
//            ShipmentRequestedEvent shipmentRequest = ShipmentCreatedEvent.newBuilder()
//                    .setOrderUuid(orderUUID.toString())
//                    .setDeliveryAddress(deliveryAddress)
//                    .setContactPhone(contactPhone)
//                    .setRequestedAt(System.currentTimeMillis())
//                    .build();

            kafkaTemplate.send("shipment-events", "shipment.requested", new byte[0]);

            log.info("배송 요청 이벤트 발행: orderUUID={}", orderUUID);

        } catch (Exception e) {
            log.error("배송 요청 이벤트 발행 실패: orderUUID={}, error={}", orderUUID, e.getMessage());
            throw new RuntimeException("배송 요청 이벤트 발행 실패", e);
        }
    }


    // 이벤트 발행 - 주문 취소
    public void publishOrderCancelled(UUID orderUUID, String reason) {
        try {
//            OrderCancelledEvent cancelEvent = OrderCancelledEvent.newBuilder()
//                    .setOrderUuid(orderUUID.toString())
//                    .setCancelledDt(System.currentTimeMillis())
//                    .build();

            kafkaTemplate.send("order-events", "order.cancelled",  new byte[0]);

            log.info("주문 취소 이벤트 발행: orderUUID={}, reason={}", orderUUID, reason);

        } catch (Exception e) {
            log.error("주문 취소 이벤트 발행 실패: orderUUID={}, error={}", orderUUID, e.getMessage());
            throw new RuntimeException("주문 취소 이벤트 발행 실패", e);
        }
    }


    // 보상 트랜잭션 - 결제 취소 요청 이벤트 발행
    public void publishPaymentCancellationRequested(UUID orderUUID) {
        try {
//            PaymentCancellationRequestedEvent cancelRequest = PaymentCancellationRequestedEvent.newBuilder()
//                    .setOrderUuid(orderUUID.toString())
//                    .setRequestedAt(System.currentTimeMillis())
//                    .build();

//            cancelRequest.toByteArray()
            kafkaTemplate.send("payment-events", "payment.cancellation.requested", new byte[0]);

            log.info("결제 취소 요청 이벤트 발행: orderUUID={} ", orderUUID);

        } catch (Exception e) {
            log.error("결제 취소 요청 이벤트 발행 실패: orderUUID={}, error={}", orderUUID, e.getMessage());
            throw new RuntimeException("결제 취소 요청 이벤트 발행 실패", e);
        }
    }

}
