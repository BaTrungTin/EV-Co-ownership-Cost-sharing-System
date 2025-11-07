package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.EContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EContractRepository extends JpaRepository<EContract, Long> {
    boolean existsByGroupId(Long groupId);
}
