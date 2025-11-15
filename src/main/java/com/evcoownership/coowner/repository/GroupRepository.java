package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    
    // Eager load ownershipShares và user trong ownershipShares để tránh LazyInitializationException
    // Sử dụng @EntityGraph với nested path
    @EntityGraph(attributePaths = {"ownershipShares.user", "createdBy"})
    @Override
    List<Group> findAll();
    
    @EntityGraph(attributePaths = {"ownershipShares.user", "createdBy"})
    @Override
    Optional<Group> findById(Long id);
    
    // Alternative: Sử dụng JOIN FETCH query để đảm bảo load đúng cách
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.ownershipShares os LEFT JOIN FETCH os.user")
    List<Group> findAllWithMembers();
    
    // Tìm groups mà user là creator
    @EntityGraph(attributePaths = {"ownershipShares.user", "createdBy"})
    @Query("SELECT g FROM Group g WHERE g.createdBy.id = :userId")
    List<Group> findByCreatedById(Long userId);
    
    // Load group với createdBy để frontend có thể check owner
    @EntityGraph(attributePaths = {"ownershipShares.user", "createdBy"})
    @Query("SELECT g FROM Group g WHERE g.id = :id")
    Optional<Group> findByIdWithCreatedBy(Long id);
}


