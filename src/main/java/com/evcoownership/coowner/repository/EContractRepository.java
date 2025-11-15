package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.EContract;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EContractRepository extends JpaRepository<EContract, Long> {
    // Eager load group và createdBy để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"group", "createdBy"})
    Optional<EContract> findByContractNo(String contractNo);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    List<EContract> findByGroupId(Long groupId);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    List<EContract> findByGroupIdAndStatus(Long groupId, String status);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    @Override
    List<EContract> findAll();
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    @Override
    Optional<EContract> findById(Long id);
}

