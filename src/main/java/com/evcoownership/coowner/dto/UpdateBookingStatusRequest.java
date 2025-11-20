package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateBookingStatusRequest {
    @NotBlank
    private String status; // PENDING, CONFIRMED, CANCELLED

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}






