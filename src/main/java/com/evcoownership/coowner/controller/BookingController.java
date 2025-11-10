package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.dto.UpdateBookingStatusRequest;
import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    public BookingController(BookingService bookingService, SecurityUtils securityUtils) {
        this.bookingService = bookingService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<Booking> create(@Validated @RequestBody CreateBookingRequest req) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(bookingService.create(req, currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> list(@RequestParam(required = false) Long vehicleId,
                                              @RequestParam(required = false) String status) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser.getId(), status, vehicleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(bookingService.cancel(id, currentUser.getId()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateStatus(@PathVariable Long id,
                                                @Validated @RequestBody UpdateBookingStatusRequest req) {
        return ResponseEntity.ok(bookingService.updateStatus(id, req.getStatus()));
    }

    @GetMapping("/priority")
    public ResponseEntity<Map<String, Object>> getPriority(@RequestParam Long groupId) {
        User currentUser = securityUtils.getCurrentUser();
        double priority = bookingService.calculateBookingPriority(currentUser.getId(), groupId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", currentUser.getId());
        result.put("groupId", groupId);
        result.put("priorityScore", priority);
        result.put("priorityDescription", priority > 0.1 ? "Cao" : priority > -0.1 ? "Trung bình" : "Thấp");
        return ResponseEntity.ok(result);
    }
}


