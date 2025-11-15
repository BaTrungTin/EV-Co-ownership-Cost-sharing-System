package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {
    @NotNull
    private Long userId;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private double percentage;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}


