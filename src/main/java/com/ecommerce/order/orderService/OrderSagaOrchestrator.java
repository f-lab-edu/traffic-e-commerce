package com.ecommerce.order.orderService;

import com.ecommerce.order.event.saga.OrderSagaEventPublisher;
import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderEntity.OrderSaga;
import com.ecommerce.order.orderRepository.OrderRepository;
import com.ecommerce.order.orderRepository.OrderSagaRepository;
import com.ecommerce.order.status.*;
import com.ecommerce.order.status.OrderStatus;
import com.ecommerce.order.status.SagaStep;
import com.ecommerce.proto.*;
import com.ecommerce.shipment.domain.ExternalShippingStatus;
import com.ecommerce.shipment.domain.Shipment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderSagaRepository orderSagaRepository;
    private final OrderSagaEventPublisher sagaEventPublisher;

    private static final int MAX_RETRY_COUNT = 3;

    // 결제 우선 플로우
    public void startOrderSaga(Order order) {
        log.info("[Orchestrator] start order : orderUUID={}", order.getOrderUUID());

        OrderSaga saga = OrderSaga.builder()
                .orderUUID(order.getOrderUUID())
                .sagaStatus(SagaStatus.STARTED)
                .currentStep(SagaStep.STOCK)
                .paymentStatus(PaymentStatus.PENDING)
                .productStatus(ProductStatus.PENDING)
                .shipmentStatus(ShipmentStatus.PENDING)
                .build();

        orderSagaRepository.save(saga);

        // 주문 상태를 INVENTORY_REQUESTED(재고 확인 요청)로 변경
        order.updateStatus(OrderStatus.INVENTORY_REQUESTED);

        requestStockCheck(order, saga);
    }

    public void cancelOrderSaga(UUID orderUUID) {
        sagaEventPublisher.publishOrderCancelled(orderUUID);
    }

    private void requestStockCheck(Order order, OrderSaga saga) {
        try {
            log.info("[Orchestrator] request stock qty check : orderUUID={}", order.getOrderUUID());

            saga.requestStockQtyChecked();
            orderSagaRepository.save(saga);

            // 재고 차감 요청 이벤트 발행
            sagaEventPublisher.publishStockCheckRequested(
                    order.getOrderUUID(),
                    order.getOrderItems()
            );

        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] fail to request stock qty check : " + e.getMessage());
        }
    }

    private void requestStockDeduction(Order order, OrderSaga saga) {
        try {
            log.info("[Orchestrator] request Stock deduction completed: orderUUID={}", order.getOrderUUID());

            saga.execStockDeduction();
            orderSagaRepository.save(saga);

            // 재고 차감 요청 이벤트 발행
            sagaEventPublisher.publishStockDeductRequested(
                    order.getOrderUUID(),
                    order.getOrderItems()
            );


        } catch (Exception e) {
            throw new RuntimeException("[Orchestrator] fail to request Stock deduction : " + e.getMessage());
        }
    }

    // 재고 차감 성공 처리
    public void handleStockDeductSuccess(StockDeductEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.info("[Orchestrator] Stock deduction success : orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 사가 상태 업데이트
        saga.completeStockDeduction();
        order.updateStatus(OrderStatus.INVENTORY_CONFIRMED);

        orderSagaRepository.save(saga);
        orderRepository.save(order);

        // 결제 요청
        requestPayment(order, saga);
    }

    // 재고 부족 처리
    public void handleStockLacked(StockDeductLackedEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.info("[Orchestrator] stock lacked : orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 재고 부족으로 보상 트랜잭션 시작
        saga.failStockDeduction();
        order.updateStatus(OrderStatus.INVENTORY_CANCELLED);

        orderSagaRepository.save(saga);
        orderRepository.save(order);
        // 보상 트랜잭션: 결제 취소 요청
        compensatePayment(order, saga);
    }


    // 결제 요청
    private void requestPayment(Order order, OrderSaga saga) {
        try {
            log.info("[Orchestrator] request payment : orderUUID={}", order.getOrderUUID());
            saga.startPayment();
            orderSagaRepository.save(saga);

            // 결제 요청 이벤트 발행
            sagaEventPublisher.publishPaymentRequested(
                    order.getOrderUUID(),
                    order.getUserId(),
                    order.getTotalPrice(),
                    order.getAddress(),
                    order.getContact()
            );

        } catch (Exception e) {

            throw new RuntimeException("[Orchestrator] fail to request payment : " + e.getMessage());
        }
    }


    public void handleStockRestored(StockRestoredEvent stockRestored) {

    }


    public void handleStockBulkDeducted(StockDeductEvent stockBulkDeducted) {

    }

    // consume 결제 성공
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.info("[Orchestrator] payment success : orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 사가 상태 업데이트
        saga.completePayment();
        order.updateStatus(OrderStatus.PAID);

        orderSagaRepository.save(saga);
        orderRepository.save(order);


        // 다음 단계: 배송 요청
        requestShipment(order, saga);
    }

    // 결제 실패 처리
    public void handlePaymentFailed(PaymentFailedEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.error("결제 실패 처리: orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 결제 실패로 사가 종료
        saga.failPayment();
        order.updateStatus(OrderStatus.CANCELLED);

        orderSagaRepository.save(saga);
        orderRepository.save(order);

        log.info("주문 사가 실패 (결제 실패): orderUUID={}", orderUUID);
    }

    public void requestShipment(Order order, OrderSaga saga) {
        log.info("[Orchestrator] request shipment : orderUUID={}", order.getOrderUUID());

        saga.startShipment();
        sagaEventPublisher.publishShipmentRequested( order.getOrderUUID(), order.getUserId(), order.getAddress(),  order.getContact(), order.getContact(), order.getOrderItems());
    }


    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.error("결제 취소 처리: orderUUID={}", orderUUID);
    }


    private void compensatePayment(Order order, OrderSaga saga) {
        try {
            saga.execCompensation();
            orderSagaRepository.save(saga);

            // 결제 취소 요청 이벤트 발행
            sagaEventPublisher.publishPaymentCancellationRequested(order.getOrderUUID());

            log.info("결제 취소 요청 완료: orderUUID={}", order.getOrderUUID());

        } catch (Exception e) {
            log.error("결제 취소 요청 실패: orderUUID={}, error={}", order.getOrderUUID(), e.getMessage());
            throw new RuntimeException("결제 취소 요청 실패: " + e.getMessage());
        }
    }


    private OrderSaga findSagaByOrderUUID(UUID orderUUID) {
        return orderSagaRepository.findByOrderUUID(orderUUID)
                .orElseThrow(() -> new RuntimeException("사가를 찾을 수 없습니다: " + orderUUID));
    }

    private Order findOrderByUUID(UUID orderUUID) {
        return orderRepository.findByOrderUUID(orderUUID)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderUUID));
    }

}
