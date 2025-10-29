package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.service.BookingService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
        @Valid @RequestBody CreateBookingRequest request
    ) {
        Booking newBooking = bookingService.createNewBooking(request);
        return ResponseEntity.ok(newBooking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getBookingsByVehicle(
        @RequestParam Long vehicleId,
        @RequestParam(required = false) LocalDateTime startTime,
        @RequestParam(required = false) LocalDateTime endTime
    ) {
        List<Booking> bookings = bookingService.findBookings(vehicleId, startTime, endTime);
        return ResponseEntity.ok(bookings);
    }
}