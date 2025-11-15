package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.*;
import com.evcoownership.coowner.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final UsageHistoryRepository usageHistoryRepository;
    private final OwnershipShareRepository ownershipShareRepository;
    private final GroupRepository groupRepository;

    public ReportService(ExpenseRepository expenseRepository,
                        ExpenseShareRepository expenseShareRepository,
                        UsageHistoryRepository usageHistoryRepository,
                        OwnershipShareRepository ownershipShareRepository,
                        GroupRepository groupRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.ownershipShareRepository = ownershipShareRepository;
        this.groupRepository = groupRepository;
    }

    public void verifyUserCanAccessGroupReport(Long groupId, Long userId) {
        // Check if user is member of the group (has ownership share) or is creator
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        
        // Check if user is admin - admin can access all groups
        // We need to get user from UserRepository to check roles
        // For now, we'll check membership first, then check if user is creator
        boolean isMember = ownershipShareRepository.existsByGroupIdAndUserId(groupId, userId);
        boolean isCreator = group.getCreatedBy() != null && group.getCreatedBy().getId().equals(userId);
        
        if (!isMember && !isCreator) {
            throw new IllegalArgumentException("Bạn không phải là member của nhóm này");
        }
    }

    public Map<String, Object> getGroupFinancialReport(Long groupId, LocalDate startDate, LocalDate endDate) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));

        List<Expense> expenses = expenseRepository.findByGroupIdAndDateBetween(groupId, startDate, endDate);
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expensesByType = expenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getType,
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("groupId", groupId);
        report.put("groupName", group.getName());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalExpenses", totalExpenses);
        report.put("expensesByType", expensesByType);
        report.put("expenseCount", expenses.size());
        report.put("paidExpenses", expenses.stream().filter(e -> "PAID".equals(e.getStatus())).count());
        report.put("pendingExpenses", expenses.stream().filter(e -> "PENDING".equals(e.getStatus())).count());

        return report;
    }

    public Map<String, Object> getUserExpenseReport(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ExpenseShare> shares = expenseShareRepository.findByUserId(userId)
                .stream()
                .filter(share -> {
                    LocalDate expenseDate = share.getExpense().getDate();
                    return !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate);
                })
                .toList();

        BigDecimal totalOwed = shares.stream()
                .map(ExpenseShare::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = shares.stream()
                .map(share -> share.getPaidAmount() != null ? share.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalOwed", totalOwed);
        report.put("totalPaid", totalPaid);
        report.put("totalPending", totalOwed.subtract(totalPaid));
        report.put("expenseShareCount", shares.size());
        report.put("paidCount", shares.stream().filter(s -> "PAID".equals(s.getStatus())).count());
        report.put("pendingCount", shares.stream().filter(s -> "PENDING".equals(s.getStatus())).count());

        return report;
    }

    public Map<String, Object> compareUsageVsOwnership(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));

        List<OwnershipShare> ownershipShares = ownershipShareRepository.findByGroupId(groupId);

        Map<String, Object> comparison = new HashMap<>();
        List<Map<String, Object>> userComparisons = new java.util.ArrayList<>();

        for (OwnershipShare share : ownershipShares) {
            Long userId = share.getUser().getId();
            double ownershipPercentage = share.getPercentage();

            // Tính usage percentage trong 30 ngày
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            
            // Tính usage của user này
            List<UsageHistory> userUsages = usageHistoryRepository
                    .findByUserIdAndStartTimeBetween(userId, oneMonthAgo, LocalDateTime.now());

            long userUsageMinutes = userUsages.stream()
                    .mapToLong(u -> {
                        if (u.getStartTime() != null && u.getEndTime() != null) {
                            return java.time.Duration.between(u.getStartTime(), u.getEndTime()).toMinutes();
                        }
                        return 0;
                    })
                    .sum();

            // Tính tổng usage của tất cả users trong group
            long allGroupUsageMinutes = 0;
            for (OwnershipShare os : ownershipShares) {
                List<UsageHistory> usages = usageHistoryRepository
                        .findByUserIdAndStartTimeBetween(os.getUser().getId(), oneMonthAgo, LocalDateTime.now());
                long minutes = usages.stream()
                        .mapToLong(u -> {
                            if (u.getStartTime() != null && u.getEndTime() != null) {
                                return java.time.Duration.between(u.getStartTime(), u.getEndTime()).toMinutes();
                            }
                            return 0;
                        })
                        .sum();
                allGroupUsageMinutes += minutes;
            }

            double usagePercentage = allGroupUsageMinutes > 0 
                ? (double) userUsageMinutes / allGroupUsageMinutes 
                : 0.0;

            Map<String, Object> userComp = new HashMap<>();
            userComp.put("userId", userId);
            userComp.put("userName", share.getUser().getFullName());
            userComp.put("ownershipPercentage", ownershipPercentage * 100);
            userComp.put("usagePercentage", usagePercentage * 100);
            userComp.put("difference", (ownershipPercentage * 100) - (usagePercentage * 100));
            userComp.put("isFair", Math.abs((ownershipPercentage * 100) - (usagePercentage * 100)) < 10); // Chênh lệch < 10%

            userComparisons.add(userComp);
        }

        comparison.put("groupId", groupId);
        comparison.put("groupName", group.getName());
        comparison.put("userComparisons", userComparisons);

        return comparison;
    }
}

