package com.ecommerce.order.orderController;

import com.ecommerce.order.dto.orderRequest.OrderCreateRequest;
import com.ecommerce.order.dto.orderResponse.OrderResponse;
import com.ecommerce.order.orderService.OrderService;
import com.ecommerce.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class orderController {

    private final JwtService jwtService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestHeader("Authorization") String auth
            , @RequestBody @Validated OrderCreateRequest request) {
        String token = auth.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserId(token));
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping("/{orderUUID}")
    public ResponseEntity<OrderResponse> detail(@PathVariable UUID orderUUID) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderUUID));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> userOrders(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwtService.extractUserId(token));
        return ResponseEntity.ok(orderService.getOrderListByUser(userId));
    }

    @PatchMapping("/{orderUUID}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable UUID orderUUID) {
        return ResponseEntity.ok(orderService.cancelOrder(orderUUID));
    }

}
