package com.evcoownership.coowner.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "groups")
public class Group 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id; 

    @Column(name = "group_name", nullable = false, unique = true)
    private String groupName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "vehicle_info", nullable = false)
    private String vehicleInfo;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OwnershipShare> ownershipShare = new ArrayList<> ();

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getGroupName() { return groupName; }

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getVehicleInfo() { return vehicleInfo; }

    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public Long getCreatedBy() { return createdBy; }

    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OwnershipShare> getOwnershipShare() { return ownershipShare; }

    public void setOwnershipShare(List<OwnershipShare> ownershipShare) { this.ownershipShare = ownershipShare; }
}
