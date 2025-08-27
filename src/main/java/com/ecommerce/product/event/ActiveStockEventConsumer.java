package com.ecommerce.product.event;

import com.ecommerce.product.dto.request.StockDeductRequest;
import com.ecommerce.product.productService.ProductService;
import com.ecommerce.proto.StockDeductEvent;
import com.ecommerce.proto.StockRestoredEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveStockEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "product-events", groupId = "product-service")
    public void consumeOrderEvents(ConsumerRecord<String, byte[]> record, Acknowledgment ack) {
        try {
            String key = record.key();
            byte[] value = record.value();
            log.info("Stock consume: order key={}", key);

            switch (key) {
                case "stock.check"  :  operateStockCheck(value);
                case "stock.deduct" :  operateStockDeduction(value);
                case "stock.restore":  operateStockRestoration(value);
                break;
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Stock error : consume order event {}", e.getMessage());
        }
    }

    private void operateStockCheck(byte[] value) {

        try {
            StockDeductEvent deductEvent = StockDeductEvent.parseFrom(value);

            List<StockDeductRequest> checkRequest = deductEvent.getItemsList().stream()
                    .map(item -> new StockDeductRequest(UUID.fromString(item.getProductUuid()), item.getDeductQuantity()))
                    .collect(Collectors.toList());

            productService.hasStockActive(checkRequest);
        } catch (InvalidProtocolBufferException e) {
            log.error("Parsing stock check error: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Stock check error: {}", e.getMessage());
        }
    }

    private void operateStockDeduction(byte[] value) {
        try {
            StockDeductEvent deductEvent = StockDeductEvent.parseFrom(value);
            UUID orderUUID = UUID.fromString(deductEvent.getOrderUuid());

            List<StockDeductRequest> deductReqList = deductEvent.getItemsList().stream()
                    .map(item -> new StockDeductRequest(UUID.fromString(item.getProductUuid()), item.getDeductQuantity()))
                    .collect(Collectors.toList());
            productService.deductStocks(deductReqList, orderUUID);

        } catch (InvalidProtocolBufferException e) {
            log.error("Parsing deduction error: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Stock deduction error: {}", e.getMessage());
        }
    }

    private void operateStockRestoration(byte[] value) {
        try {
            StockRestoredEvent restoreEvent = StockRestoredEvent.parseFrom(value);
            UUID orderUUID = UUID.fromString(restoreEvent.getOrderUuid());

            List<StockDeductRequest> restoreReqList = restoreEvent.getItemsList().stream()
                    .map(item -> new StockDeductRequest(UUID.fromString(item.getProductUuid()), item.getRestoredQuantity()))
                    .collect(Collectors.toList());
            productService.restoreStocks(restoreReqList, orderUUID);

        } catch (InvalidProtocolBufferException e) {
            log.error("Parsing restoration error: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Stock restoration error: {}", e.getMessage());
        }
    }

}