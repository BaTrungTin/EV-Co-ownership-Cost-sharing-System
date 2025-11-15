package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.OwnershipShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipShareRepository extends JpaRepository<OwnershipShare, Long>
{
    Optional<OwnershipShare> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<OwnershipShare> findByGroupId(Long groupId);
    List<OwnershipShare> findAllByGroupId(Long groupId);
    double sumPercentageByGroupId(Long groupId);
}
