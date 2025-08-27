package com.ecommerce.product.event;


import com.ecommerce.product.dto.response.StockDeductResult;
import com.ecommerce.proto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveStockEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    // 주문 여부 확인
    public void publishStockActive(boolean active) {
        try {

            StockDeductEvent event = StockDeductEvent.newBuilder()
//                    .setOrderUuid(orderUUID.toString())
//                    .addItems(DeductItems.newBuilder()
//                            .setProductUuid(productUUID.toString())
//                            .setDeductQuantity(quantity)
//                            .setRemainingStock(remainingStock)
//                            .build())
//                    .setDeductDt(System.currentTimeMillis())
                    .build();



            kafkaTemplate.send("product-events", "stock.check.response", event.toByteArray());
//            log.info("Publish stock active item: orderUUID={}, productUUID={}", orderUUID, productUUID);
        } catch (Exception e) {
            log.error("Fail : publishing stock active {}", e.getMessage());
        }

    }


    // 재고 차감 성공
    public void publishStockDeduct(UUID orderUUID, UUID productUUID, int quantity, int remainingStock) {
        try {
            StockDeductEvent event = StockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .addItems(DeductItems.newBuilder()
                            .setProductUuid(productUUID.toString())
                            .setDeductQuantity(quantity)
                            .setRemainingStock(remainingStock)
                            .build())
                    .setDeductDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("product-events", "stock.deducted", event.toByteArray());
            log.info("Publish deduct item: orderUUID={}, productUUID={}", orderUUID, productUUID);
        } catch (Exception e) {
            log.error("Fail : publishing stock deduct {}", e.getMessage());
        }

    }

    // 재고 부족 이벤트
    public void publishStockLacked(UUID orderUUID, UUID productUUID, int requestedQuantity, int availableStock) {
        try {
            StockDeductLackedEvent event = StockDeductLackedEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .addItems(LackedItems.newBuilder()
                            .setProductUuid(productUUID.toString())
                            .setRequestedQuantity(requestedQuantity)
                            .setAvailableStock(availableStock)
                            .build())
                    .setCheckedDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("product-events", "stock.lacked", event.toByteArray());
            log.info("Publish stock lacked: orderUUID={}, productUUID={}", orderUUID, productUUID);
        } catch (Exception e) {
            log.error("Fail : publishing stock lacked {}", e.getMessage());
        }
    }

    // 재고 복구
    public void publishStockRestored(UUID orderUUID, UUID productUUID, int quantity, int currentStock) {
        try {
            StockRestoredEvent event = StockRestoredEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .addItems(RestoredItems.newBuilder()
                            .setProductUuid(productUUID.toString())
                            .setRestoredQuantity(quantity)
                            .setCurrentStock(currentStock)
                            .build())
                    .setRestoredDt(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send("product-events", "stock.restored", event.toByteArray());
            log.info("Publish stock restored: orderUUID={}, productUUID={}", orderUUID, productUUID);
        } catch (Exception e) {
            log.error("Fail : publishing stock restored {}", e.getMessage());
        }
    }


    // 배치 재고 차감
    public void publishBulkStockDeduct(UUID orderUUID, List<StockDeductResult> results) {
        try {
            StockDeductEvent.Builder eventBuilder = StockDeductEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setDeductDt(System.currentTimeMillis());

            for (StockDeductResult result : results) {
                if (result.isSuccess()) {
                    eventBuilder.addItems(DeductItems.newBuilder()
                            .setProductUuid(result.getProductUUID().toString())
                            .setDeductQuantity(result.getRemainingStock())
                            .setRemainingStock(result.getRemainingStock())
                            .build());
                }
            }

            kafkaTemplate.send("product-events", "stock.bulk.deducted", eventBuilder.build().toByteArray());
            log.info("배치 재고 차감 이벤트 발행: 주문={}", orderUUID);
        } catch (Exception e) {
            log.error("Fail : publishing batch stock deduct {}", e.getMessage());
        }
    }

    // 배치 재고 차감 실패
    public void publishBulkStockFailed(UUID orderUUID, List<StockDeductResult> results) {
        try {
            StockDeductLackedEvent.Builder eventBuilder = StockDeductLackedEvent.newBuilder()
                    .setOrderUuid(orderUUID.toString())
                    .setCheckedDt(System.currentTimeMillis());

            for (StockDeductResult result : results) {
                if (!result.isSuccess()) {
                    eventBuilder.addItems(LackedItems.newBuilder()
                            .setProductUuid(result.getProductUUID().toString())
                            .setRequestedQuantity(0)
                            .setAvailableStock(result.getRemainingStock())
                            .build());
                }
            }

            kafkaTemplate.send("product-events", "stock.lacked", eventBuilder.build().toByteArray());
            log.info("배치 재고 실패 이벤트 발행: 주문={}", orderUUID);
        } catch (Exception e) {
            log.error("Fail : publishing batch stock failed {}", e.getMessage());
        }
    }


}
