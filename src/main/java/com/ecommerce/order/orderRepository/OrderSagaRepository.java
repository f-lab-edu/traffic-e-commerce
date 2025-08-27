package com.ecommerce.order.orderRepository;

import com.ecommerce.order.orderEntity.OrderSaga;
import com.ecommerce.order.status.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, Long> {
    
    Optional<OrderSaga> findByOrderUUID(UUID orderUUID);
    List<OrderSaga> findBySagaStatus(SagaStatus sagaStatus);
    List<OrderSaga> findBySagaStatusAndRetryCountLessThan(SagaStatus sagaStatus, Integer maxRetry);
    
}
