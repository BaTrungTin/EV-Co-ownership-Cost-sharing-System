package com.evcoownership.coowner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"vote_id", "user_id"}))
public class UserVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vote_id")
    @JsonIgnoreProperties({"userVotes", "group", "createdBy"}) // Tránh circular reference
    private Vote vote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "roles"}) // Không trả về password và roles để tránh LazyInitializationException
    private User user;

    @Column(nullable = false)
    private String choice; // YES, NO, OPTION_1, ...

    @Column(nullable = false)
    private LocalDateTime votedAt;

    public Long getId() { return id; }
    public Vote getVote() { return vote; }
    public void setVote(Vote vote) { this.vote = vote; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}

