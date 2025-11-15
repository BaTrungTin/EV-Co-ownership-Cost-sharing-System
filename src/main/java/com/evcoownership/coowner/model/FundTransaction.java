package com.evcoownership.coowner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_transactions")
public class FundTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fund_id")
    @JsonIgnoreProperties({"transactions"}) // Tránh circular reference
    private CommonFund fund;

    @Column(nullable = false)
    private String type; // DEPOSIT (nộp vào), WITHDRAW (rút ra)

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "roles"}) // Không trả về password và roles để tránh LazyInitializationException
    private User createdBy;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String reference; // Tham chiếu đến expense hoặc payment

    public Long getId() { return id; }
    public CommonFund getFund() { return fund; }
    public void setFund(CommonFund fund) { this.fund = fund; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}

