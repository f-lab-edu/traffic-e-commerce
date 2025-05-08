package com.ecommerce.order.orderService;

import com.ecommerce.order.dto.orderRequest.OrderCreateRequest;
import com.ecommerce.order.dto.orderResponse.OrderItemResponse;
import com.ecommerce.order.dto.orderResponse.OrderResponse;
import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderEntity.OrderItem;
import com.ecommerce.order.orderRepository.OrderItemRepository;
import com.ecommerce.order.orderRepository.OrderRepository;
import com.ecommerce.proto.EdaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;


    public OrderResponse createOrder(UUID userId, OrderCreateRequest request) {


        BigDecimal totalPrice = request.getItems().stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        Order createdOrder = Order.of(userId, request, totalPrice, request.getItems());
        Order savedOrder = orderRepository.save(createdOrder);

        // 카프카 메세지 ProtoBuf 직렬화
        byte[] eventByte = toCreateOrderProtoBuf(savedOrder);
        kafkaTemplate.send("order-events", "order.created", eventByte);


        return buildResponse(savedOrder);
    }

    public OrderResponse getOrderDetail(UUID orderUUID) {
        Order order = orderRepository.findByOrderUUID(orderUUID).orElseThrow(() -> new IllegalArgumentException("Cannot find order"));
        return buildResponse(order);
    }

    public List<OrderResponse> getOrderListByUser(UUID userId) {
        return orderRepository.findAllByUserId(userId).stream().map(this::buildResponse).toList();
    }

    public OrderResponse cancelOrder(UUID orderUUID) {
        Order order = orderRepository.findByOrderUUID(orderUUID).orElseThrow(() -> new IllegalArgumentException("Cannot find order"));

        // 주문 상태만 변경
        order.cancel();
        Order cancelledOrder = orderRepository.save(order);

        // 카프카 메세지 ProtoBuf 직렬화
        byte[] eventByte = toCreateOrderProtoBuf(cancelledOrder);
        kafkaTemplate.send("order-events", "order.cancelled", eventByte);

        return buildResponse(cancelledOrder);
    }


    private OrderResponse buildResponse(Order order) {
        return OrderResponse.builder()
                .orderUUID(order.getOrderUUID())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .address(order.getAddress())
                .contact(order.getContact())
                .items(order.getOrderItems().stream().map(i ->
                        OrderItemResponse.builder()
                                .productUUID(i.getProductUUID())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build()
                ).toList())
                .build();
    }

    private byte[] toCreateOrderProtoBuf(Order order) {
        var protoBuilder = EdaMessage.OrderCreatedEvent.newBuilder()
                .setOrderUUID(order.getOrderUUID().toString())
                .setUserId(order.getUserId().toString())
                .setAddress(order.getAddress())
                .setContact(order.getContact())
                .setTotalPrice(order.getTotalPrice().doubleValue())
                .setStatus(order.getStatus().name());


        for (OrderItem item : order.getOrderItems()) {
            protoBuilder.addItems(

                    EdaMessage.OrderItem.newBuilder()
                            .setProductUUID(item.getProductUUID().toString())
                            .setQuantity(item.getQuantity())
                            .setPrice(item.getPrice().doubleValue())
                            .build()
            );
        }
        return protoBuilder.build().toByteArray();

    }


}
