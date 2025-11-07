package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.model.EContract;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.Vehicle;
import com.evcoownership.coowner.repository.BookingRepository;
import com.evcoownership.coowner.repository.VehicleRepository;
import com.evcoownership.coowner.repository.UserRepository; // (Giả sử bạn có)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private VehicleRepository vehicleRepository; 
    @Autowired
    private UserRepository userRepository; 

    public Booking createNewBooking(CreateBookingRequest request) {

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        User currentUser = userRepository.findById(1L).get(); 
        
        EContract group = currentUser.getGroup();

        if (!vehicle.getGroup().getId().equals(group.getId())) {
            throw new RuntimeException("User does not belong to this vehicle's group");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
            vehicle.getId(),
            request.getStartTime(),
            request.getEndTime()
        );

        if (isOverlapping) {
            throw new RuntimeException("This time slot is already booked for this vehicle");
        }

        Booking newBooking = new Booking();
        newBooking.setVehicle(vehicle);
        newBooking.setUser(currentUser);
        newBooking.setGroup(group);
        newBooking.setStartTime(request.getStartTime());
        newBooking.setEndTime(request.getEndTime());
        newBooking.setStatus("pending");

        return bookingRepository.save(newBooking);
    }

    public List<Booking> findBookings(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return bookingRepository.findByVehicle_IdAndStartTimeAfterAndEndTimeBefore(
                vehicleId, startTime, endTime
            );
        } else {
            return bookingRepository.findByVehicle_Id(vehicleId);
        }
    }
}