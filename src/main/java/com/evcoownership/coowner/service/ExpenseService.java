package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateExpenseRequest;
import com.evcoownership.coowner.model.*;
import com.evcoownership.coowner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final GroupRepository groupRepository;
    private final VehicleRepository vehicleRepository;
    private final OwnershipShareRepository ownershipShareRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                         ExpenseShareRepository expenseShareRepository,
                         GroupRepository groupRepository,
                         VehicleRepository vehicleRepository,
                         OwnershipShareRepository ownershipShareRepository,
                         UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.groupRepository = groupRepository;
        this.vehicleRepository = vehicleRepository;
        this.ownershipShareRepository = ownershipShareRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Expense createExpense(CreateExpenseRequest req, Long userId) {
        Group group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setCreatedBy(user);
        if (req.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new IllegalArgumentException("Vehicle không tồn tại"));
            expense.setVehicle(vehicle);
        }
        expense.setType(req.getType());
        expense.setAmount(req.getAmount());
        expense.setDate(req.getDate());
        expense.setDescription(req.getDescription());
        expense.setSplitMethod(req.getSplitMethod());
        expense.setStatus("PENDING");

        Expense savedExpense = expenseRepository.save(expense);

        // Tự động chia chi phí
        splitExpense(savedExpense);

        return savedExpense;
    }

    private void splitExpense(Expense expense) {
        List<OwnershipShare> shares = ownershipShareRepository.findByGroupId(expense.getGroup().getId());
        
        if (shares.isEmpty()) {
            throw new IllegalArgumentException("Group không có thành viên");
        }

        List<ExpenseShare> expenseShares = new ArrayList<>();
        BigDecimal totalAmount = expense.getAmount();

        if ("BY_OWNERSHIP".equals(expense.getSplitMethod())) {
            // Chia theo tỉ lệ sở hữu
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
            // BY_USAGE - cần implement logic tính theo mức sử dụng (có thể dùng UsageHistory)
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
        }

        expenseShareRepository.saveAll(expenseShares);
    }

    public List<Expense> getGroupExpenses(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    public List<ExpenseShare> getUserExpenseShares(Long userId) {
        return expenseShareRepository.findByUserId(userId);
    }

    @Transactional
    public Expense approveExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense không tồn tại"));
        
        // Validate user is member of group
        boolean isMember = ownershipShareRepository.existsByGroupIdAndUserId(
                expense.getGroup().getId(), userId);
        if (!isMember) {
            throw new IllegalArgumentException("User không thuộc group");
        }
        
        expense.setStatus("APPROVED");
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense rejectExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense không tồn tại"));
        
        // Validate user is member of group
        boolean isMember = ownershipShareRepository.existsByGroupIdAndUserId(
                expense.getGroup().getId(), userId);
        if (!isMember) {
            throw new IllegalArgumentException("User không thuộc group");
        }
        
        expense.setStatus("REJECTED");
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense không tồn tại"));

        // Chỉ cho phép xóa nếu user là người tạo expense
        if (!expense.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo expense mới được xóa");
        }

        // Chỉ cho phép xóa nếu expense chưa được approve hoặc chưa có payment
        if ("APPROVED".equals(expense.getStatus()) || "PAID".equals(expense.getStatus())) {
            // Kiểm tra xem có payment nào liên quan không
            List<ExpenseShare> shares = expenseShareRepository.findByExpenseId(expenseId);
            boolean hasPaidShares = shares.stream().anyMatch(s -> 
                "PAID".equals(s.getStatus()) && s.getPaidAmount() != null && 
                s.getPaidAmount().compareTo(BigDecimal.ZERO) > 0);
            
            if (hasPaidShares) {
                throw new IllegalArgumentException("Không thể xóa expense đã có payment");
            }
        }

        // Xóa expense shares (cascade delete sẽ tự động xóa nếu có cascade)
        expenseShareRepository.deleteAll(expenseShareRepository.findByExpenseId(expenseId));
        
        // Xóa expense
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public Expense getExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense không tồn tại"));
    }

    @Transactional(readOnly = true)
    public void verifyUserIsGroupMember(Long groupId, Long userId) {
        if (!ownershipShareRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("Bạn không phải là member của nhóm này");
        }
    }
}

