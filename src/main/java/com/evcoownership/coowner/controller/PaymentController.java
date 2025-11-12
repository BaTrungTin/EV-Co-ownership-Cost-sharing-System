package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.Payment;
import com.evcoownership.coowner.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> create(@RequestParam Long expenseShareId,
                                        @RequestParam Long userId,
                                        @RequestParam BigDecimal amount,
                                        @RequestParam String method) {
        return ResponseEntity.ok(paymentService.processPayment(expenseShareId, userId, amount, method));
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments(@RequestParam Long userId) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    @GetMapping("/expense-share/{expenseShareId}")
    public ResponseEntity<List<Payment>> getExpenseSharePayments(@PathVariable Long expenseShareId) {
        return ResponseEntity.ok(paymentService.getExpenseSharePayments(expenseShareId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id,
                                               @RequestParam String status,
                                               @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status, transactionId));
    }
}

