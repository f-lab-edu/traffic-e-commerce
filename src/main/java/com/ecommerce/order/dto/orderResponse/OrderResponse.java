package com.ecommerce.order.dto.orderResponse;

import com.ecommerce.order.orderStatus.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID orderUUID;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private String address;

    private String contact;

    private List<OrderItemResponse> items;

}
