package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AddMemberRequest 
{
    @NotNull(message = "ID khong duoc null!")
    private Long userId;

    @NotNull(message = "Ty le % khong duoc null!")
    @DecimalMin(value = "0.0", inclusive = false, message = "Phai lon hon 0!")
    @DecimalMax(value = "1.0", inclusive = true, message = "Phai nho hon hoac bang 1!")    
    private Double percentage;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}
