package com.evcoownership.coowner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.evcoownership.coowner.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long>
{
    Optional<Group> findByGroupName(Long groupName);
    boolean existsByGroupName(String groupName);
}
