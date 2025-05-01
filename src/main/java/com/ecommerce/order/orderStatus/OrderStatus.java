package com.ecommerce.order.orderStatus;


public enum OrderStatus {
    ORDERED // 주문생성
    , CANCELLED // 주문취소
    , PARTIALLY_CANCELLED // 부분 취소
    , PAID  // 결제 처리 요청
    , INVENTORY_CONFIRMED // 재고 확보
    , INVENTORY_CANCELLED // 재고 취소
    , SHIPPED // 배송 요청
    , DELIVERED // 배송 진행중

}
