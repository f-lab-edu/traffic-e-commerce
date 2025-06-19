package com.ecommerce.order.orderRepository;

import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderEntity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    void deleteAllByOrder(Order order);

}
