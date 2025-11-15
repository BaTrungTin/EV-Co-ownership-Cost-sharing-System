package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.OwnershipShare;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnershipShareRepository extends JpaRepository<OwnershipShare, Long> {
    @EntityGraph(attributePaths = {"user", "group.createdBy"})
    List<OwnershipShare> findByGroupId(Long groupId);
    @EntityGraph(attributePaths = {"group.createdBy", "user"})
    List<OwnershipShare> findByUserId(Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    @EntityGraph(attributePaths = {"user", "group.createdBy"})
    java.util.Optional<OwnershipShare> findByGroupIdAndUserId(Long groupId, Long userId);
}


