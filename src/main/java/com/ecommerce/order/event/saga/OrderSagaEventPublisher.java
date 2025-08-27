package com.ecommerce.order.event.saga;

import com.ecommerce.order.orderEntity.OrderItem;
import com.ecommerce.proto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    // 재고 확인 요청
    public void publishStockCheckRequested(UUID orderUUID, List<OrderItem> orderItems) {
        try {
            SagaStockDeductEvent.Builder eventBuilder = SagaStockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString());


            // 주문 아이템들을
            for (OrderItem item : orderItems) {
                eventBuilder.addItems(DeductItem.newBuilder()
                        .setProductUuid(item.getProductUUID().toString())
                        .setDeductQuantity(item.getQuantity())
                        .build());
            }

            SagaStockDeductEvent event = eventBuilder.build();
            kafkaTemplate.send("product-events", "stock.check", event.toByteArray());

            log.info("[Orchestrator] Publish stock check : orderUUID={}, itemCount={}", orderUUID, orderItems.size());
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - publish stock qty check : "+ e);
        }
    }




    // 재고 차감 요청
    public void publishStockDeductRequested(UUID orderUUID, List<OrderItem> orderItems) {
        try {
            SagaStockDeductEvent.Builder eventBuilder = SagaStockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setDeductDt(System.currentTimeMillis());

            // 주문 아이템들을 재고 차감 아이템으로 변환
            for (OrderItem item : orderItems) {
                eventBuilder.addItems(DeductItem.newBuilder()
                        .setProductUuid(item.getProductUUID().toString())
                        .setDeductQuantity(item.getQuantity())
                        .setRemainingStock(0)
                        .build());
            }

            SagaStockDeductEvent event = eventBuilder.build();
            kafkaTemplate.send("product-events", "stock.deduct", event.toByteArray());

            log.info("[Orchestrator] Publish stock deduction : orderUUID={}, itemCount={}", orderUUID, orderItems.size());
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - publish deduct stock "+ e);
        }
    }

    // 재고 복구 요청
    public void publishStockRestoreRequested(UUID orderUUID, List<OrderItem> orderItems, String reason) {
        try {
            SagaStockRestoreEvent.Builder eventBuilder = SagaStockRestoreEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setRestoredDt(System.currentTimeMillis());

            for (OrderItem item : orderItems) {
                eventBuilder.addItems(RestoreItem.newBuilder()
                        .setProductUuid(item.getProductUUID().toString())
                        .setRestoredQuantity(item.getQuantity())
                        .setCurrentStock(0) // 요청 시에는 0으로 설정
                        .build());
            }

            SagaStockRestoreEvent event = eventBuilder.build();
            kafkaTemplate.send("order-event", "stock.restore", event.toByteArray());

            log.info("[Orchestrator] Publish stock restoration : orderUUID={}, itemCount={}", orderUUID, orderItems.size());
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - publish stock restore stock", e);
        }
    }


    // 결제 요청 이벤트 발행
    public void publishPaymentRequested(UUID orderUUID, UUID userId, BigDecimal totalAmount, String contactPhone) {
        try {

            SagaPaymentRequestEvent paymentRequest = SagaPaymentRequestEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setTotalPrice(totalAmount.doubleValue())
                    .setRequestedDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("payment-events", "saga.payment.request", paymentRequest.toByteArray());
            log.info("[Orchestrator] publish to request payment : orderUUID={}, amount={}", orderUUID, totalAmount);
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - publish deduct stock ");
        }
    }


    // 이벤트 발행 - 배송 요청
    public void publishShipmentRequested(UUID orderUUID, UUID userId, String address, String contact, List<OrderItem> orderItems) {
        try {
            SagaShipmentRequestEvent.Builder shipmentRequest = SagaShipmentRequestEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setUserId(userId.toString())
                    .setDeliveryAddress(address)
                    .setContactPhone(contact)
                    .setRequestedDt(System.currentTimeMillis());



            SagaStockDeductEvent.Builder eventBuilder = SagaStockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString());


            // 주문 아이템들 넣기
            for (OrderItem item : orderItems) {
                shipmentRequest.addItems(ShipmentItem.newBuilder()
                    .setProductUuid(item.getProductUUID().toString())
                    .setQuantity(item.getQuantity())
                    .setPrice(item.getPrice().doubleValue())
                    .build());
            }
            // 250803 : 배송 요청 까지 진행. 배송쪽 불필요한 퍼블리셔 있는지 체크. 성공된 배송 퍼블리싱후 사가에서 후속처리되는 프로세스 만들기
            SagaShipmentRequestEvent event = shipmentRequest.build();

            kafkaTemplate.send("shipment-events", "shipment.requested", event.toByteArray());
            log.info("[Orchestrator] publish to request shipment : orderUUID={}", orderUUID);
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - request ship  "+ e.getMessage());
        }
    }


    // 이벤트 발행 - 주문 취소
    public void publishOrderCancelled(UUID orderUUID) {
        try {
            OrderCancelledEvent cancelledEvent = OrderCancelledEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setCancelledDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("order-events", "order.cancelled",  cancelledEvent.toByteArray());
            log.info("[Orchestrator] publish cancel order : orderUUID={}", orderUUID);
        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] Exception - publish cancel order "+ e.getMessage());
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
