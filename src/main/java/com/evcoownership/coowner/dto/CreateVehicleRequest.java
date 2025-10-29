package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateVehicleRequest {
    @NotBlank
    @Size(min = 5, max = 20)
    private String vin;

    @NotBlank
    @Size(min = 5, max = 20)
    private String plate;

    @NotBlank
    private String model;

    @NotNull
    private Long groupId;

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}
