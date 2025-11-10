package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.ExpenseShare;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    // Eager load expense và user để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"expense", "user"})
    List<ExpenseShare> findByExpenseId(Long expenseId);
    
    @EntityGraph(attributePaths = {"expense", "user"})
    List<ExpenseShare> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"expense", "user"})
    List<ExpenseShare> findByUserIdAndStatus(Long userId, String status);
    
    @EntityGraph(attributePaths = {"expense", "user"})
    Optional<ExpenseShare> findByExpenseIdAndUserId(Long expenseId, Long userId);
    
    @EntityGraph(attributePaths = {"expense", "user"})
    @Override
    List<ExpenseShare> findAll();
    
    @EntityGraph(attributePaths = {"expense", "user"})
    @Override
    Optional<ExpenseShare> findById(Long id);
}

