package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateGroupRequest 
{
    @NotBlank(message = "Ten nhom khong duoc de trong!")
    private String name;

    @NotNull(message = "ID nguoi tao khong duoc null!")
    private Long createdBy;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
