package com.ecommerce.order.orderRepository;

import com.ecommerce.order.orderEntity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByOrderUUID(UUID uuid);

    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllByUserId(UUID userId);

    void deleteById(Long id);

}
