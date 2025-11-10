package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.Vehicle;
import com.evcoownership.coowner.repository.BookingRepository;
import com.evcoownership.coowner.repository.VehicleRepository;
import com.evcoownership.coowner.repository.UserRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository; 
    private final UserRepository userRepository; 

    // Constructor
    public BookingService(BookingRepository bookingRepository, 
                          VehicleRepository vehicleRepository, 
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    public Booking createNewBooking(CreateBookingRequest request, Long userId) {

        User currentUser = findUserOrThrow(userId);
        Vehicle vehicle = findVehicleOrThrow(request.getVehicleId());

        validateBookingLogic(request, currentUser, vehicle);

        Booking newBooking = buildBookingEntity(request, currentUser, vehicle);
        return bookingRepository.save(newBooking);
    }

    private void validateBookingLogic(CreateBookingRequest request, User user, Vehicle vehicle) {
        // Kiểm tra thời gian hợp lệ (start phải trước end)
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Kiểm tra người dùng có thuộc nhóm của xe không
        validateGroupMembership(user, vehicle);
        
        // Kiểm tra xe có bị trùng lịch không
        validateAvailability(vehicle.getId(), request.getStartTime(), request.getEndTime());
    }

    private Booking buildBookingEntity(CreateBookingRequest request, User user, Vehicle vehicle) {
        Booking newBooking = new Booking();
        newBooking.setVehicle(vehicle);
        newBooking.setUser(user);
        newBooking.setGroup(vehicle.getGroup()); 
        newBooking.setStartTime(request.getStartTime());
        newBooking.setEndTime(request.getEndTime());
        newBooking.setStatus("pending"); 
        return newBooking;
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(" Không tìm thấy người dùng"));
    }

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(" Không tìm thấy xe")); 
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(" Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }

    private void validateGroupMembership(User user, Vehicle vehicle) {
        Group vehicleGroup = vehicle.getGroup();
        Group userGroup = user.getGroup();

        if (vehicleGroup == null || userGroup == null || !vehicleGroup.getId().equals(userGroup.getId())) {
            throw new IllegalArgumentException(" Người dùng không thuộc nhóm sở hữu xe này");
        }
    }

    private void validateAvailability(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
            vehicleId,
            startTime,
            endTime
        );

        if (isOverlapping) { 
            throw new IllegalArgumentException(" Xe đã được đặt trong khoảng thời gian này");
        }
    }


    //tìm kiếm và lọc các lượt đặt xe.
    //GET /api/bookings?vehicleId=&startTime=&endTime=
    public List<Booking> findBookings(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        
        if (startTime != null && endTime != null) {
            return bookingRepository.findByVehicleIdAndStartTimeAfterAndEndTimeBefore(
                vehicleId, startTime, endTime
            );
        } else {
            return bookingRepository.findByVehicleId(vehicleId);
        }
    }
    
}