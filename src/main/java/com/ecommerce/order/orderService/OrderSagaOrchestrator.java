package com.ecommerce.order.orderService;

import com.ecommerce.order.event.OrderEventPublisher;
import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderEntity.OrderSaga;
import com.ecommerce.order.orderRepository.OrderRepository;
import com.ecommerce.order.orderRepository.OrderSagaRepository;
import com.ecommerce.order.status.*;
import com.ecommerce.payment.event.model.PaymentFailedEvent;
import com.ecommerce.payment.event.model.PaymentSuccessEvent;
import com.ecommerce.product.dto.request.ProductDeleteRequest;
import com.ecommerce.proto.StockDeductEvent;
import com.ecommerce.proto.StockDeductLackedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderSagaRepository orderSagaRepository;
    private final OrderEventPublisher orderEventPublisher;

    private static final int MAX_RETRY_COUNT = 3;

    // 결제 우선 플로우
    public void startOrderSaga(Order order) {
        log.info("주문 사가 시작: orderUUID={}", order.getOrderUUID());


        OrderSaga saga = OrderSaga.builder()
                .orderUUID(order.getOrderUUID())
                .sagaStatus(SagaStatus.STARTED)
                .currentStep(SagaStep.PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .productStatus(ProductStatus.PENDING)
                .shipmentStatus(ShipmentStatus.PENDING)
                .build();

        orderSagaRepository.save(saga);

        // 주문 상태를 PAYMENT_REQUESTED로 변경
//        order.updateStatus(OrderStatus.PAYMENT_REQUESTED);
//        orderRepository.save(order);

        // 결제 요청부터 시작
        requestPayment(order, saga);
    }


    // 결제 요청
    private void requestPayment(Order order, OrderSaga saga) {
        try {
            saga.startPayment();
            orderSagaRepository.save(saga);

            // 결제 요청 이벤트 발행
            orderEventPublisher.publishPaymentRequested(
                    order.getOrderUUID(),
                    order.getUserId(),
                    order.getTotalPrice(),
                    order.getAddress(),
                    order.getContact()
            );

            log.info("결제 요청 완료: orderUUID={}", order.getOrderUUID());

        } catch (Exception e) {
            log.error("결제 요청 실패: orderUUID={}, error={}", order.getOrderUUID(), e.getMessage());
            throw new RuntimeException("결제 요청 실패: " + e.getMessage());
        }
    }


    // 결제 성공 처리
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        UUID orderUUID = event.getOrderUUID();
        log.info("결제 성공 처리: orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 사가 상태 업데이트
//        saga.completePayment();
//        order.updateStatus(OrderStatus.PAID);

        orderSagaRepository.save(saga);
        orderRepository.save(order);

        // 다음 단계: 재고 차감 요청
//        requestInventoryDeduction(order, saga);
    }


    // 결제 실패 처리
    public void handlePaymentFailure(PaymentFailedEvent event) {
        UUID orderUUID = event.getOrderId();
        log.error("결제 실패 처리: orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 결제 실패로 사가 종료
        saga.failPayment(event.getFailure());
//        order.updateStatus(OrderStatus.CANCELLED);

        orderSagaRepository.save(saga);
        orderRepository.save(order);

        log.info("주문 사가 실패 (결제 실패): orderUUID={}", orderUUID);
    }

    private void requestInventoryDeduction(Order order, OrderSaga saga) {
        try {
            saga.startInventoryDeduction();
            orderSagaRepository.save(saga);

            // 재고 차감 요청 이벤트 발행
            orderEventPublisher.publishInventoryDeductionRequested(
                    order.getOrderUUID(),
                    order.getOrderItems()
            );

            log.info("재고 차감 요청 완료: orderUUID={}", order.getOrderUUID());

        } catch (Exception e) {
            log.error("재고 차감 요청 실패: orderUUID={}, error={}", order.getOrderUUID(), e.getMessage());
            throw new RuntimeException("재고 차감 요청 실패: " + e.getMessage());
        }
    }


    // 재고 차감 성공 처리
    public void handleInventoryDeductionSuccess(StockDeductEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.info("재고 차감 성공 처리: orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 사가 상태 업데이트
//        saga.completeInventoryDeduction();
//        order.updateStatus(OrderStatus.INVENTORY_CONFIRMED);

        orderSagaRepository.save(saga);
        orderRepository.save(order);

        // 다음 단계: 배송 요청
//        requestShipment(order, saga);
    }

    // 재고 부족 처리
    public void handleInventoryInsufficient(StockDeductLackedEvent event) {
        UUID orderUUID = UUID.fromString(event.getOrderUuid());
        log.error("재고 부족 처리: orderUUID={}", orderUUID);

        OrderSaga saga = findSagaByOrderUUID(orderUUID);
        Order order = findOrderByUUID(orderUUID);

        // 재고 부족으로 보상 트랜잭션 시작
//        saga.failInventoryDeduction("재고 부족");
//        order.updateStatus(OrderStatus.INVENTORY_CANCELLED);

//        orderSagaRepository.save(saga);
//        orderRepository.save(order);

        // 보상 트랜잭션: 결제 취소 요청
        compensatePayment(order, saga);
    }

    private void compensatePayment(Order order, OrderSaga saga) {
        try {
            saga.startCompensation();
            orderSagaRepository.save(saga);

            // 결제 취소 요청 이벤트 발행
            orderEventPublisher.publishPaymentCancellationRequested(order.getOrderUUID());

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
