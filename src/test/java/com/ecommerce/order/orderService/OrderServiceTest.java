package com.ecommerce.order.orderService;

import com.ecommerce.order.dto.orderRequest.OrderCreateRequest;
import com.ecommerce.order.dto.orderRequest.OrderItemRequest;
import com.ecommerce.order.dto.orderResponse.OrderResponse;
import com.ecommerce.order.orderEntity.Order;
import com.ecommerce.order.orderRepository.OrderItemRepository;
import com.ecommerce.order.orderRepository.OrderRepository;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.proto.EdaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private KafkaConsumer<String, byte[]> consumer;
    private List<UUID> createOrderUUID = new ArrayList<>();

    @BeforeEach
    void SetUp() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "service-test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("order-events"));

    }

    @AfterEach
    void cleanTestData() {
        for (UUID orderUUID : createOrderUUID) {
            try {
                Order order = orderRepository.findByOrderUUID(orderUUID).orElse(null);
                orderItemRepository.deleteAllByOrder(order);
                orderRepository.delete(order);
            } catch (Exception e) {
                log.error("fail to delete order {}", e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("create order - save DB and publish message to kafka")
    public void createOrderTest() {

        UUID userId = UUID.randomUUID();
        List<OrderItemRequest> itemRequests = Arrays.asList(
                OrderItemRequest.builder().productUUID(UUID.randomUUID())
                        .quantity(10)
                        .price(new BigDecimal("50.00"))
                        .build(),
                OrderItemRequest.builder().productUUID(UUID.randomUUID())
                        .quantity(74)
                        .price(new BigDecimal("153.00"))
                        .build()
        );

        OrderCreateRequest createRequest = new OrderCreateRequest(itemRequests, "seoul", "010-8483-4895");

        OrderResponse response = orderService.createOrder(userId, createRequest);
        createOrderUUID.add(response.getOrderUUID());

        then(response).isNotNull();
        then(response.getOrderUUID()).isNotNull();
        then(response.getStatus()).isEqualTo(OrderStatus.ORDERED);
        then(response.getTotalPrice()).isEqualTo(new BigDecimal("203.00"));
        then(response.getItems().size()).isEqualTo(2);

        Optional<Order> savedOrder = orderRepository.findByOrderUUID(response.getOrderUUID());
        then(savedOrder).isPresent();
        then(savedOrder.get().getUserId()).isEqualTo(userId);
        then(savedOrder.get().getOrderItems().size()).isEqualTo(2);
        then(savedOrder.get().getAddress()).isEqualTo("seoul");
        then(savedOrder.get().getContact()).isEqualTo("010-8483-4895");

        AtomicBoolean messageReceived = new AtomicBoolean(false);
        AtomicReference<EdaMessage.OrderCreatedEvent> receivedEvent = new AtomicReference<>();

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    consumer.poll(Duration.ofMillis(500))
                            .forEach(data -> {
                                if ("order.created".equals(data.key())) {
                                    try {
                                        EdaMessage.OrderCreatedEvent event = EdaMessage.OrderCreatedEvent.parseFrom(data.value());
                                        if (response.getOrderUUID().toString().equals(event.getOrderUUID())) {
                                            receivedEvent.set(event);
                                            messageReceived.set(true);
                                        }
                                    } catch (Exception e) {
                                        fail("fail to parse protobuf message : {}", e.getMessage());
                                    }
                                }
                            });


                    return messageReceived.get();
                });

        EdaMessage.OrderCreatedEvent event = receivedEvent.get();
        then(event).isNotNull();
        then(event.getUserId()).isEqualTo(userId.toString());
        then(event.getAddress()).isEqualTo(createRequest.getAddress());
        then(event.getContact()).isEqualTo(createRequest.getContact());
        then(event.getTotalPrice()).isEqualTo(response.getTotalPrice());
        then(event.getItemsCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("get orders in detail")
    public void getOrderDetailTest() {

        UUID userId = UUID.randomUUID();
        OrderItemRequest itemRequests =
                OrderItemRequest.builder().productUUID(UUID.randomUUID())
                        .quantity(1)
                        .price(new BigDecimal("10000.00"))
                        .build();

        OrderItemRequest itemRequests2 =
                OrderItemRequest.builder().productUUID(UUID.randomUUID())
                        .quantity(3)
                        .price(new BigDecimal("100.00"))
                        .build();


        OrderItemRequest itemRequests3 =
                OrderItemRequest.builder().productUUID(UUID.randomUUID())
                        .quantity(10)
                        .price(new BigDecimal("390.00"))
                        .build();

        // 3900 + 300 + 10000


        OrderCreateRequest createRequest = new OrderCreateRequest(
                Arrays.asList(itemRequests, itemRequests2, itemRequests3)
                , "Gwacheon"
                , "010-1111-2345"
        );

        OrderResponse createOrder = orderService.createOrder(userId, createRequest);

        OrderResponse orderDetail = orderService.getOrderDetail(createOrder.getOrderUUID());

        then(orderDetail).isNotNull();
        then(orderDetail.getOrderUUID()).isNotNull();
        then(orderDetail.getStatus()).isEqualTo(OrderStatus.ORDERED);
        then(orderDetail.getTotalPrice()).isEqualTo(new BigDecimal("14200.00"));
        then(orderDetail.getItems().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("get orders in detail")
    public void getOrderListByUserTest() {

        UUID userId = UUID.fromString("c3c7310e-bedb-43e5-b18c-0ae745279397");

        List<OrderResponse> orderListByUser = orderService.getOrderListByUser(userId);

        then(orderListByUser).isNotNull();
        then(orderListByUser.get(0).getItems().size()).isEqualTo(1);
    }


    @Test
    @DisplayName("cancel order and validate published event from kafka")
    public void cancelOrderTest() {
        UUID userId = UUID.randomUUID();
        OrderCreateRequest createRequest = new OrderCreateRequest(
                List.of(
                        OrderItemRequest.builder()
                                .productUUID(UUID.randomUUID())
                                .quantity(41)
                                .price(new BigDecimal("100.00"))
                                .build(),
                          OrderItemRequest.builder()
                                .productUUID(UUID.randomUUID())
                                .quantity(332)
                                .price(new BigDecimal("200.00"))
                                .build(),
                           OrderItemRequest.builder()
                                .productUUID(UUID.randomUUID())
                                .quantity(12)
                                .price(new BigDecimal("300.00"))
                                .build(),
                          OrderItemRequest.builder()
                                .productUUID(UUID.randomUUID())
                                .quantity(25)
                                .price(new BigDecimal("400.00"))
                                .build(),
                          OrderItemRequest.builder()
                                .productUUID(UUID.randomUUID())
                                .quantity(679)
                                .price(new BigDecimal("4460.00"))
                                .build()
                ),
                "Test Address",
                "010-3452-1123"
        );

        // 주문 생성
        OrderResponse createdOrder = orderService.createOrder(userId, createRequest);

        // 주문 취소 (주문 UUID 사용)
        OrderResponse cancelledOrder = orderService.cancelOrder(createdOrder.getOrderUUID());

        then(cancelledOrder).isNotNull();
        then(cancelledOrder.getItems().size()).isEqualTo(4);

    }


}
