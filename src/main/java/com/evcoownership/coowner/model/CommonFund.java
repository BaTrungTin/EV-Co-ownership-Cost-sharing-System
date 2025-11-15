package com.evcoownership.coowner.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "common_funds")
public class CommonFund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private String fundType; // MAINTENANCE_RESERVE (quỹ bảo dưỡng), EMERGENCY (phí dự phòng), OTHER

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance; // Số dư hiện tại

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(length = 1000)
    private String description;

    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public String getFundType() { return fundType; }
    public void setFundType(String fundType) { this.fundType = fundType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

