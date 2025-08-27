package com.ecommerce.order.orderEntity;

import com.ecommerce.order.dto.orderRequest.OrderCreateRequest;
import com.ecommerce.order.dto.orderRequest.OrderItemRequest;
import com.ecommerce.order.dto.orderResponse.OrderItemResponse;
import com.ecommerce.order.status.OrderStatus;
import com.ecommerce.proto.OrderCreatedEvent;
import com.ecommerce.proto.OrderCreatedItem;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Order of(OrderCreatedEvent event) {

        List<OrderItem> orderItems = event.getItemsList().stream().map(item -> OrderItem.builder()
                .productUUID(UUID.fromString(item.getProductUUID()))
                .quantity(item.getQuantity())
                .price(BigDecimal.valueOf(item.getPrice()))
                .build()
        ).toList();

        return Order.builder()
                .orderUUID(UUID.fromString(event.getOrderUUID()))
                .userId(UUID.fromString(event.getUserId()))
                .address(event.getAddress())
                .contact(event.getContact())
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(event.getTotalPrice()))
                .orderItems(orderItems)
                .build();

    }


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.orderUUID = UUID.randomUUID();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

}
