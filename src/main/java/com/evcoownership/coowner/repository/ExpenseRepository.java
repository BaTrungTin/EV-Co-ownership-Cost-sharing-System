package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Eager load group, vehicle, createdBy để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    List<Expense> findByGroupId(Long groupId);
    
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    List<Expense> findByGroupIdAndStatus(Long groupId, String status);
    
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    List<Expense> findByGroupIdAndDateBetween(Long groupId, LocalDate startDate, LocalDate endDate);
    
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    List<Expense> findByVehicleId(Long vehicleId);
    
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    @Override
    List<Expense> findAll();
    
    @EntityGraph(attributePaths = {"group", "vehicle", "createdBy"})
    @Override
    java.util.Optional<Expense> findById(Long id);
}

