package com.ecommerce.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";
    @Value("${spring.kafka.bootstrap-server}")
    private String bootStrapServers;
    @Value("${spring.kafka.consumer.group-id:ecommerce-service}")
    private String defaultGroupId;

    @Bean
    public ProducerFactory<String, byte[]> producerFactory() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        configMap.put(ProducerConfig.ACKS_CONFIG, "all");  // 모든 복제본이 메시지를 받았는지 확인
        configMap.put(ProducerConfig.RETRIES_CONFIG, 3);   // 실패 시 재시도 횟수
        configMap.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // 재시도 간격

        return new DefaultKafkaProducerFactory<>(configMap);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 주문 서비스용 컨슈머 팩토리
     */
    @Bean
    public ConsumerFactory<String, byte[]> orderConsumerFactory() {
        return createConsumerFactory(ORDER_EVENTS_TOPIC);
    }

    /**
     * 상품 서비스용 컨슈머 팩토리
     */
    @Bean
    public ConsumerFactory<String, byte[]> productConsumerFactory() {
        return createConsumerFactory(PRODUCT_EVENTS_TOPIC);
    }

    /**
     * 결제 서비스용 컨슈머 팩토리
     */
    @Bean
    public ConsumerFactory<String, byte[]> paymentConsumerFactory() {
        return createConsumerFactory(PAYMENT_EVENTS_TOPIC);
    }

    /**
     * 배송 서비스용 컨슈머 팩토리
     */
    @Bean
    public ConsumerFactory<String, byte[]> deliveryConsumerFactory() {
        return createConsumerFactory(DELIVERY_EVENTS_TOPIC);
    }


    private ConsumerFactory<String, byte[]> createConsumerFactory(String groupId) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> orderKafkaListenerContainerFactory() {
        return createListenerContainerFactory(orderConsumerFactory());
    }


    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> productKafkaListenerContainerFactory() {
        return createListenerContainerFactory(productConsumerFactory());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> paymentKafkaListenerContainerFactory() {
        return createListenerContainerFactory(paymentConsumerFactory());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> deliveryKafkaListenerContainerFactory() {
        return createListenerContainerFactory(deliveryConsumerFactory());
    }

    private ConcurrentKafkaListenerContainerFactory<String, byte[]> createListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);


        return factory;
    }

}
