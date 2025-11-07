package com.evcoownership.coowner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateBookingRequest {

    @Schema(description = "ID của xe (Vehicle) cần đặt", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;

    @Schema(description = "Thời gian bắt đầu đặt", example = "2025-01-10T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start time cannot be null")
    @Future
    private LocalDateTime startTime;

    @Schema(description = "Thời gian kết thúc đặt", example = "2025-01-10T11:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "End time cannot be null")
    @Future
    private LocalDateTime endTime;

    public CreateBookingRequest() {
    }

    public CreateBookingRequest(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}