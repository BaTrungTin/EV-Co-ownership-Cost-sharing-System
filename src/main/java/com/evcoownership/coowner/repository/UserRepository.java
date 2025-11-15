package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Eager load roles để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"roles"})
    @Override
    Optional<User> findById(Long id);
    
    @EntityGraph(attributePaths = {"roles"})
    @Override
    List<User> findAll();
    
    // Alternative: Sử dụng JOIN FETCH query để đảm bảo load đúng cách
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();
}


