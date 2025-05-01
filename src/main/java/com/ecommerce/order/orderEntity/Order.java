package com.ecommerce.order.orderEntity;

import com.ecommerce.order.dto.orderRequest.OrderCreateRequest;
import com.ecommerce.order.dto.orderRequest.OrderItemRequest;
import com.ecommerce.order.orderStatus.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_uuid", columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID orderUUID;

    @Column(name = "user_uuid", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    private String address;

    private String contact;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "crea_dt")
    private LocalDateTime createdAt;

    @Column(name = "updt_dt")
    private LocalDateTime updatedAt;

    public static Order of(UUID userId, OrderCreateRequest dto, BigDecimal totalPrice, List<OrderItemRequest> itemRequests) {
        Order order = Order.builder()
                .userId(userId).address(dto.getAddress()).contact(dto.getContact()).status(OrderStatus.ORDERED).totalPrice(totalPrice)
                .build();

        List<OrderItem> orderItems = itemRequests.stream()
                .map(item -> OrderItem.builder()
                        .order(order).productUUID(item.getProductUUID()).quantity(item.getQuantity()).price(item.getPrice())
                        .build())
                .toList();

        order.orderItems = orderItems;
        return order;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.orderUUID = UUID.randomUUID();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

}
