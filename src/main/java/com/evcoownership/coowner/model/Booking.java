package com.evcoownership.coowner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.Group;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(optional = false) 
    @JoinColumn(name = "group_id")
    private Group group; 

    @ManyToOne(optional = false) 
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "user_id")
    private User user; 

    @Column(nullable = false) 
    private LocalDateTime startTime;

    @Column(nullable = false) 
    private LocalDateTime endTime;

    @Column(nullable = false) 
    private String status; 

    public Long getId() { return id; } 
    public Group getGroup() { return group; } 
    public void setGroup(Group group) { this.group = group; } 
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}