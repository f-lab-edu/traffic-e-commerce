package com.ecommerce.payment.paymentRepository;

import com.ecommerce.payment.paymentDomain.Payment;
import com.ecommerce.payment.paymentDomain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderUUID(UUID orderUUID);

    List<Payment> findByOrderUUIDAndStatus(UUID orderUUID, PaymentStatus status);
}
