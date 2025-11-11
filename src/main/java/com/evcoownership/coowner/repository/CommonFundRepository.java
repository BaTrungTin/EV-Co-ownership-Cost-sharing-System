package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.CommonFund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommonFundRepository extends JpaRepository<CommonFund, Long> {
    List<CommonFund> findByGroupId(Long groupId);
    Optional<CommonFund> findByGroupIdAndFundType(Long groupId, String fundType);
}

