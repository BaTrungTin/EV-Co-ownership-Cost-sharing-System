package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByExpenseShareId(Long expenseShareId);
    List<Payment> findByUserIdAndStatus(Long userId, String status);
    List<Payment> findByStatus(String status);
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

