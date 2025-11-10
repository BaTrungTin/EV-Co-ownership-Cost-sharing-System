package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findByVoteId(Long voteId);
    Optional<VoteOption> findByVoteIdAndOption(Long voteId, String option);
}

