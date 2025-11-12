package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.Expense;
import com.evcoownership.coowner.model.ExpenseShare;
import com.evcoownership.coowner.model.OwnershipShare;
import com.evcoownership.coowner.repository.ExpenseShareRepository;
import com.evcoownership.coowner.repository.OwnershipShareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseShareService {
    private final ExpenseShareRepository expenseShareRepository;
    private final OwnershipShareRepository ownershipShareRepository;

    public ExpenseShareService(ExpenseShareRepository expenseShareRepository,
                               OwnershipShareRepository ownershipShareRepository) {
        this.expenseShareRepository = expenseShareRepository;
        this.ownershipShareRepository = ownershipShareRepository;
    }

    @Transactional
    public List<ExpenseShare> createExpenseShares(Expense expense) {
        List<OwnershipShare> shares = ownershipShareRepository.findByGroupId(expense.getGroup().getId());

        if (shares.isEmpty()) {
            throw new IllegalArgumentException("Group không có thành viên");
        }

        List<ExpenseShare> expenseShares = new ArrayList<>();
        BigDecimal totalAmount = expense.getAmount();

        if ("BY_OWNERSHIP".equals(expense.getSplitMethod())) {
            // Chia theo tỉ lệ sở hữu
            // Validate tổng percentage = 1.0
            double totalPercentage = shares.stream()
                    .mapToDouble(OwnershipShare::getPercentage)
                    .sum();

            if (Math.abs(totalPercentage - 1.0) > 0.01) {
                throw new IllegalArgumentException("Tổng tỉ lệ sở hữu phải bằng 1.0, hiện tại: " + totalPercentage);
            }

            for (OwnershipShare share : shares) {
                ExpenseShare expenseShare = new ExpenseShare();
                expenseShare.setExpense(expense);
                expenseShare.setUser(share.getUser());
                BigDecimal userAmount = totalAmount.multiply(BigDecimal.valueOf(share.getPercentage()))
                        .setScale(2, RoundingMode.HALF_UP);
                expenseShare.setAmount(userAmount);
                expenseShare.setStatus("PENDING");
                expenseShare.setPaidAmount(BigDecimal.ZERO);
                expenseShares.add(expenseShare);
            }
        } else if ("EQUAL".equals(expense.getSplitMethod())) {
            // Chia đều
            if (shares.size() == 0) {
                throw new IllegalArgumentException("Group phải có ít nhất 1 thành viên");
            }

            BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(shares.size()), 2, RoundingMode.HALF_UP);
            for (OwnershipShare share : shares) {
                ExpenseShare expenseShare = new ExpenseShare();
                expenseShare.setExpense(expense);
                expenseShare.setUser(share.getUser());
                expenseShare.setAmount(perPerson);
                expenseShare.setStatus("PENDING");
                expenseShare.setPaidAmount(BigDecimal.ZERO);
                expenseShares.add(expenseShare);
            }
        } else if ("BY_USAGE".equals(expense.getSplitMethod())) {
            // BY_USAGE - sẽ implement logic tính theo mức sử dụng (Day 5 với UsageHistory)
            // Tạm thời chia đều
            BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(shares.size()), 2, RoundingMode.HALF_UP);
            for (OwnershipShare share : shares) {
                ExpenseShare expenseShare = new ExpenseShare();
                expenseShare.setExpense(expense);
                expenseShare.setUser(share.getUser());
                expenseShare.setAmount(perPerson);
                expenseShare.setStatus("PENDING");
                expenseShare.setPaidAmount(BigDecimal.ZERO);
                expenseShares.add(expenseShare);
            }
        } else {
            throw new IllegalArgumentException("Split method không hợp lệ: " + expense.getSplitMethod());
        }

        return expenseShareRepository.saveAll(expenseShares);
    }

    public List<ExpenseShare> getUserExpenseShares(Long userId) {
        return expenseShareRepository.findByUserId(userId);
    }

    public List<ExpenseShare> getExpenseSharesByExpenseId(Long expenseId) {
        return expenseShareRepository.findByExpenseId(expenseId);
    }

    public List<ExpenseShare> getUserExpenseSharesByStatus(Long userId, String status) {
        return expenseShareRepository.findByUserIdAndStatus(userId, status);
    }

    @Transactional
    public void deleteExpenseSharesByExpenseId(Long expenseId) {
        List<ExpenseShare> shares = expenseShareRepository.findByExpenseId(expenseId);
        expenseShareRepository.deleteAll(shares);
    }
}

