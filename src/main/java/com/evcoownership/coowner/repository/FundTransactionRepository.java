package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.FundTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {
    // Eager load fund và createdBy để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"fund", "createdBy"})
    List<FundTransaction> findByFundId(Long fundId);
    
    @EntityGraph(attributePaths = {"fund", "createdBy"})
    List<FundTransaction> findByFundIdOrderByTransactionDateDesc(Long fundId);
    
    @EntityGraph(attributePaths = {"fund", "createdBy"})
    List<FundTransaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    
    @EntityGraph(attributePaths = {"fund", "createdBy"})
    @Override
    List<FundTransaction> findAll();
    
    @EntityGraph(attributePaths = {"fund", "createdBy"})
    @Override
    java.util.Optional<FundTransaction> findById(Long id);
}

