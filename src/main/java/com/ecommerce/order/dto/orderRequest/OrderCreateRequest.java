package com.ecommerce.order.dto.orderRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    private List<OrderItemRequest> items;

    private String address;

    private String contact;

}
