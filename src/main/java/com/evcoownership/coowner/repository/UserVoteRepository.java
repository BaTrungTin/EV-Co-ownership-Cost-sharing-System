package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.UserVote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    // Eager load vote và user để tránh LazyInitializationException
    @EntityGraph(attributePaths = {"vote", "user"})
    List<UserVote> findByVoteId(Long voteId);
    
    @EntityGraph(attributePaths = {"vote", "user"})
    List<UserVote> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"vote", "user"})
    Optional<UserVote> findByVoteIdAndUserId(Long voteId, Long userId);
    
    boolean existsByVoteIdAndUserId(Long voteId, Long userId);
    
    @EntityGraph(attributePaths = {"vote", "user"})
    @Override
    List<UserVote> findAll();
    
    @EntityGraph(attributePaths = {"vote", "user"})
    @Override
    Optional<UserVote> findById(Long id);
}

