package com.ecommerce.delivery.repository;

import com.ecommerce.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByDeliveryUUID(UUID deliveryUUID);

}
