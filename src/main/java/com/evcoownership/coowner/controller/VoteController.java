package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.UserVote;
import com.evcoownership.coowner.model.Vote;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;
    private final SecurityUtils securityUtils;

    public VoteController(VoteService voteService, SecurityUtils securityUtils) {
        this.voteService = voteService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<Vote> create(@RequestBody CreateVoteRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(voteService.createVote(
            request.getGroupId(), 
            request.getTopic(), 
            request.getDescription(), 
            request.getVotingMethod(), 
            request.getDeadline(), 
            currentUser.getId()
        ));
    }

    @PostMapping("/{voteId}/cast")
    public ResponseEntity<UserVote> castVote(@PathVariable Long voteId,
                                           @RequestBody CastVoteRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(voteService.castVote(voteId, currentUser.getId(), request.getChoice()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Vote>> getGroupVotes(@PathVariable Long groupId) {
        return ResponseEntity.ok(voteService.getGroupVotes(groupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vote> get(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.getVote(id));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Vote> closeVote(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(voteService.closeVote(id, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        voteService.deleteVote(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // Inner classes for request bodies
    public static class CreateVoteRequest {
        private Long groupId;
        private String topic;
        private String description;
        private String votingMethod = "SIMPLE_MAJORITY";
        private LocalDateTime deadline;

        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVotingMethod() { return votingMethod; }
        public void setVotingMethod(String votingMethod) { this.votingMethod = votingMethod; }
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    }

    public static class CastVoteRequest {
        private String choice;

        public String getChoice() { return choice; }
        public void setChoice(String choice) { this.choice = choice; }
    }
}

