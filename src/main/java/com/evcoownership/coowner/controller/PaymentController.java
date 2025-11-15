package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.Payment;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    public PaymentController(PaymentService paymentService, SecurityUtils securityUtils) {
        this.paymentService = paymentService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody CreatePaymentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể tạo thanh toán");
        }
        return ResponseEntity.ok(paymentService.processPayment(
            request.getExpenseShareId(), 
            currentUser.getId(), 
            request.getAmount(), 
            request.getMethod()
        ));
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments() {
        User currentUser = securityUtils.getCurrentUser();
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể xem thanh toán của người dùng");
        }
        return ResponseEntity.ok(paymentService.getUserPayments(currentUser.getId()));
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

    public static class CreatePaymentRequest {
        private Long expenseShareId;
        private BigDecimal amount;
        private String method;

        public Long getExpenseShareId() { return expenseShareId; }
        public void setExpenseShareId(Long expenseShareId) { this.expenseShareId = expenseShareId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }
}

