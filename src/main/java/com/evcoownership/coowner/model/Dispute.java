package com.evcoownership.coowner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String category; // EXPENSE, BOOKING, OWNERSHIP, PAYMENT, OTHER

    @Column(nullable = false)
    private String status; // OPEN, IN_REVIEW, RESOLVED, CLOSED

    @ManyToOne
    @JoinColumn(name = "related_booking_id")
    private Booking relatedBooking;

    @ManyToOne
    @JoinColumn(name = "related_expense_id")
    private Expense relatedExpense;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    @JsonIgnoreProperties({"password", "roles"}) // Không trả về password và roles để tránh LazyInitializationException
    private User resolvedBy; // Staff/Admin

    @Column(length = 1000)
    private String resolution;

    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Booking getRelatedBooking() { return relatedBooking; }
    public void setRelatedBooking(Booking relatedBooking) { this.relatedBooking = relatedBooking; }
    public Expense getRelatedExpense() { return relatedExpense; }
    public void setRelatedExpense(Expense relatedExpense) { this.relatedExpense = relatedExpense; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}

