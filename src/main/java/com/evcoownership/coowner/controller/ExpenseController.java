package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateExpenseRequest;
import com.evcoownership.coowner.model.Expense;
import com.evcoownership.coowner.model.ExpenseShare;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SecurityUtils securityUtils;

    public ExpenseController(ExpenseService expenseService, SecurityUtils securityUtils) {
        this.expenseService = expenseService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<Expense> create(@Validated @RequestBody CreateExpenseRequest req) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin không thể tạo expense
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể tạo chi phí");
        }
        return ResponseEntity.ok(expenseService.createExpense(req, currentUser.getId()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getGroupExpenses(@PathVariable Long groupId) {
        User currentUser = securityUtils.getCurrentUser();
        // Chỉ member của group mới được xem expenses
        expenseService.verifyUserIsGroupMember(groupId, currentUser.getId());
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }

    @GetMapping("/my-shares")
    public ResponseEntity<List<ExpenseShare>> getMyExpenseShares() {
        User currentUser = securityUtils.getCurrentUser();
        // Admin không thể xem expense shares của user
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể xem chi phí của người dùng");
        }
        return ResponseEntity.ok(expenseService.getUserExpenseShares(currentUser.getId()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Expense> approveExpense(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin không thể approve expense
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể thao tác với chi phí của người dùng");
        }
        return ResponseEntity.ok(expenseService.approveExpense(id, currentUser.getId()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Expense> rejectExpense(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin không thể reject expense
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể thao tác với chi phí của người dùng");
        }
        return ResponseEntity.ok(expenseService.rejectExpense(id, currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> get(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Expense expense = expenseService.getExpense(id);
        // Chỉ member của group mới được xem expense
        expenseService.verifyUserIsGroupMember(expense.getGroup().getId(), currentUser.getId());
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin không thể xóa expense
        if (securityUtils.isAdmin(currentUser)) {
            throw new IllegalArgumentException("Admin không thể xóa chi phí của người dùng");
        }
        expenseService.deleteExpense(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

