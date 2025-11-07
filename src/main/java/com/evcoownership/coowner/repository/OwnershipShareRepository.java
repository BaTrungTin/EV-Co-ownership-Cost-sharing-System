package com.evcoownership.coowner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.evcoownership.coowner.model.OwnershipShare;

@Repository
public interface OwnershipShareRepository extends JpaRepository<OwnershipShare, Long>
{
    Optional<OwnershipShare> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<OwnershipShare> findAllByGroupId(Long groupId);
    double sumPercentageByGroupId(Long groupId);
}
