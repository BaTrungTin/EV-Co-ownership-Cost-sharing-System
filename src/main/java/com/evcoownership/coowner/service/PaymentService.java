package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.ExpenseShare;
import com.evcoownership.coowner.model.Payment;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.ExpenseShareRepository;
import com.evcoownership.coowner.repository.PaymentRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                         ExpenseShareRepository expenseShareRepository,
                         UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Payment processPayment(Long expenseShareId, Long userId, BigDecimal amount, String method) {
        ExpenseShare expenseShare = expenseShareRepository.findById(expenseShareId)
                .orElseThrow(() -> new IllegalArgumentException("Expense share không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        if (!expenseShare.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Không phải expense share của user này");
        }

        if (amount.compareTo(expenseShare.getAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không được vượt quá số tiền phải trả");
        }

        Payment payment = new Payment();
        payment.setExpenseShare(expenseShare);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setMethod(method); 
        payment.setStatus("COMPLETED"); 
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCompletedAt(LocalDateTime.now());
        payment.setTransactionId(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newPaidAmount = (expenseShare.getPaidAmount() != null ? expenseShare.getPaidAmount() : BigDecimal.ZERO)
                .add(amount);
        expenseShare.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(expenseShare.getAmount()) >= 0) {
            expenseShare.setStatus("PAID");
        } else {
            expenseShare.setStatus("PARTIAL");
        }

        expenseShareRepository.save(expenseShare);

        return savedPayment;
    }

    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getExpenseSharePayments(Long expenseShareId) {
        return paymentRepository.findByExpenseShareId(expenseShareId);
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, String status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại"));
        payment.setStatus(status);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        if ("COMPLETED".equals(status)) {
            payment.setCompletedAt(LocalDateTime.now());
        }
        return paymentRepository.save(payment);
    }
}

