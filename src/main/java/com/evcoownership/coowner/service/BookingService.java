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

    //Hàm "quản lý" chính: tạo một booking mới.
    //POST /api/bookings
    public Booking createNewBooking(CreateBookingRequest request, Long userId) {

        // 1. LẤY DỮ LIỆU CỐT LÕI
        User currentUser = findUserOrThrow(userId);
        Vehicle vehicle = findVehicleOrThrow(request.getVehicleId());

        // 2. THỰC HIỆN TẤT CẢ KIỂM TRA LOGIC
        // Các hàm này sẽ ném ra lỗi nếu có gì đó sai.
        validateBookingLogic(request, currentUser, vehicle);

        // 3. NẾU MỌI THỨ OK -> TẠO VÀ LƯU
        Booking newBooking = buildBookingEntity(request, currentUser, vehicle);
        return bookingRepository.save(newBooking);
    }

    // CÁC HÀM HELPER (PRIVATE) ĐỂ GIÚP "RÚT GỌN" LOGIC

    //Một hàm "helper" để gom tất cả các bước kiểm tra logic lại một chỗ.

    private void validateBookingLogic(CreateBookingRequest request, User user, Vehicle vehicle) {
        // Kiểm tra 1: Thời gian hợp lệ (start phải trước end)
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Kiểm tra 2: Người dùng có thuộc nhóm của xe không
        validateGroupMembership(user, vehicle);
        
        // Kiểm tra 3: Xe có bị trùng lịch không
        validateAvailability(vehicle.getId(), request.getStartTime(), request.getEndTime());
    }


    //Xây dựng đối tượng Booking (Entity) từ DTO và các đối tượng đã lấy được.
    private Booking buildBookingEntity(CreateBookingRequest request, User user, Vehicle vehicle) {
        Booking newBooking = new Booking();
        newBooking.setVehicle(vehicle);
        newBooking.setUser(user);
        newBooking.setGroup(vehicle.getGroup()); // Lấy group từ vehicle (đã được xác thực)
        newBooking.setStartTime(request.getStartTime());
        newBooking.setEndTime(request.getEndTime());
        newBooking.setStatus("pending"); // Trạng thái mặc định khi mới tạo
        return newBooking;
    }

    // --- Các hàm kiểm tra chi tiết ---

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found")); 
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void validateGroupMembership(User user, Vehicle vehicle) {
        Group vehicleGroup = vehicle.getGroup();
        Group userGroup = user.getGroup();

        if (vehicleGroup == null || userGroup == null || !vehicleGroup.getId().equals(userGroup.getId())) {
            throw new IllegalArgumentException("User does not belong to this vehicle's group");
        }
    }

    private void validateAvailability(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
            vehicleId,
            startTime,
            endTime
        );

        if (isOverlapping) { 
            throw new IllegalArgumentException("This time slot is already booked...");
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