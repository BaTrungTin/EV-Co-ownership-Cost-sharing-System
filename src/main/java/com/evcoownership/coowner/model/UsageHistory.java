package com.evcoownership.coowner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_history")
public class UsageHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

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

    private Integer startOdometer; // Số km khi bắt đầu
    private Integer endOdometer; // Số km khi kết thúc
    private Integer distance; // Quãng đường đi được (km)

    @Column(length = 1000)
    private String notes; // Ghi chú về chuyến đi

    @ManyToOne
    @JoinColumn(name = "checked_in_by")
    private User checkedInBy; // Staff check-in

    @ManyToOne
    @JoinColumn(name = "checked_out_by")
    private User checkedOutBy; // Staff check-out

    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;

    @Column(length = 500)
    private String checkInQrCode;

    @Column(length = 500)
    private String checkOutQrCode;

    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getStartOdometer() { return startOdometer; }
    public void setStartOdometer(Integer startOdometer) { this.startOdometer = startOdometer; }
    public Integer getEndOdometer() { return endOdometer; }
    public void setEndOdometer(Integer endOdometer) { this.endOdometer = endOdometer; }
    public Integer getDistance() { return distance; }
    public void setDistance(Integer distance) { this.distance = distance; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getCheckedInBy() { return checkedInBy; }
    public void setCheckedInBy(User checkedInBy) { this.checkedInBy = checkedInBy; }
    public User getCheckedOutBy() { return checkedOutBy; }
    public void setCheckedOutBy(User checkedOutBy) { this.checkedOutBy = checkedOutBy; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
    public LocalDateTime getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(LocalDateTime checkedOutAt) { this.checkedOutAt = checkedOutAt; }
    public String getCheckInQrCode() { return checkInQrCode; }
    public void setCheckInQrCode(String checkInQrCode) { this.checkInQrCode = checkInQrCode; }
    public String getCheckOutQrCode() { return checkOutQrCode; }
    public void setCheckOutQrCode(String checkOutQrCode) { this.checkOutQrCode = checkOutQrCode; }
}

