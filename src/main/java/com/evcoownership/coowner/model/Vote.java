package com.evcoownership.coowner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties({"ownershipShares"}) // Tránh circular reference
    private Group group;

    @Column(nullable = false)
    private String topic; // UPGRADE_BATTERY, INSURANCE, SELL_VEHICLE, OTHER

    @Column(nullable = false, length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "roles"}) // Không trả về password và roles để tránh LazyInitializationException
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime deadline; // Hạn chót bỏ phiếu

    @Column(nullable = false)
    private String status; // OPEN, CLOSED, APPROVED, REJECTED

    @Column(nullable = false)
    private String votingMethod; // SIMPLE_MAJORITY (đa số đơn giản), UNANIMOUS (nhất trí), OWNERSHIP_WEIGHTED (theo tỉ lệ sở hữu)

    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVotingMethod() { return votingMethod; }
    public void setVotingMethod(String votingMethod) { this.votingMethod = votingMethod; }
}

