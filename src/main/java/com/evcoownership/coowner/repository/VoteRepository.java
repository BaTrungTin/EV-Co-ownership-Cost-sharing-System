package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Vote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    // Eager load group và createdBy để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"group", "createdBy"})
    List<Vote> findByGroupId(Long groupId);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    List<Vote> findByGroupIdAndStatus(Long groupId, String status);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    List<Vote> findByDeadlineBeforeAndStatus(LocalDateTime deadline, String status);
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    @Override
    List<Vote> findAll();
    
    @EntityGraph(attributePaths = {"group", "createdBy"})
    @Override
    java.util.Optional<Vote> findById(Long id);
}

