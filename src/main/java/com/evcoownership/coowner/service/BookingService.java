package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateBookingRequest;
import com.evcoownership.coowner.exception.ForbiddenException;
import com.evcoownership.coowner.model.*;
import com.evcoownership.coowner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final OwnershipShareRepository ownershipShareRepository;

    public BookingService(BookingRepository bookingRepository,
                          VehicleRepository vehicleRepository,
                          UserRepository userRepository,
                          OwnershipShareRepository ownershipShareRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.ownershipShareRepository = ownershipShareRepository;
    }

    @Transactional
    public Booking create(CreateBookingRequest req, Long userId) {
        // Validate request
        if (req.getVehicleId() == null) {
            throw new IllegalArgumentException("Vehicle ID không được để trống");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (req.getEndTime().isBefore(req.getStartTime()) || req.getEndTime().isEqual(req.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        
        // Load vehicle với group (EntityGraph sẽ eager load group)
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle không tồn tại với ID: " + req.getVehicleId()));
        
        // Get group from vehicle (should be loaded by EntityGraph)
        Group group = vehicle.getGroup();
        if (group == null) {
            throw new IllegalArgumentException("Vehicle không thuộc group nào");
        }
        
        // Validate time is in future (additional check besides @Future)
        LocalDateTime now = LocalDateTime.now();
        if (req.getStartTime().isBefore(now) || req.getStartTime().isEqual(now)) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trong tương lai");
        }
        if (req.getEndTime().isBefore(now) || req.getEndTime().isEqual(now)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải trong tương lai");
        }
        
        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại với ID: " + userId));
        
        // Check if user is member of group
        boolean isMember = ownershipShareRepository.existsByGroupIdAndUserId(group.getId(), user.getId());
        if (!isMember) {
            throw new IllegalArgumentException("User không thuộc group của vehicle. User ID: " + userId + ", Group ID: " + group.getId());
        }
        
        // Check conflict với status = CONFIRMED hoặc PENDING (bỏ qua CANCELLED)
        List<Booking> overlaps = bookingRepository.findByVehicleId(vehicle.getId())
                .stream()
                .filter(b -> (b.getStatus().equals("CONFIRMED") || b.getStatus().equals("PENDING"))
                        && isOverlap(b.getStartTime(), b.getEndTime(), req.getStartTime(), req.getEndTime()))
                .toList();
        
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Trùng lịch với booking khác. Có " + overlaps.size() + " booking đã tồn tại trong khoảng thời gian này");
        }
        
        // Create booking
        Booking b = new Booking();
        b.setVehicle(vehicle);
        b.setGroup(group);
        b.setUser(user);
        b.setStartTime(req.getStartTime());
        b.setEndTime(req.getEndTime());
        b.setStatus("CONFIRMED");
        return bookingRepository.save(b);
    }

    private boolean isOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // Tính điểm ưu tiên: ownership% - usage% (trong 30 ngày gần đây)
    public double calculateBookingPriority(Long userId, Long groupId) {
        OwnershipShare ownership = ownershipShareRepository.findByGroupIdAndUserId(groupId, userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User không có ownership trong group"));

        double ownershipPercentage = ownership.getPercentage();

        // Tính usage percentage trong 30 ngày
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Booking> userBookings = bookingRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getStatus().equals("CONFIRMED") 
                        && b.getStartTime().isAfter(thirtyDaysAgo))
                .toList();

        long userUsageMinutes = userBookings.stream()
                .mapToLong(b -> java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                .sum();

        // Lấy tất cả bookings của group trong 30 ngày
        List<Booking> allGroupBookings = bookingRepository.findAll()
                .stream()
                .filter(b -> b.getStatus().equals("CONFIRMED") 
                        && b.getStartTime().isAfter(thirtyDaysAgo)
                        && b.getGroup().getId().equals(groupId))
                .collect(Collectors.toList());

        long totalGroupUsageMinutes = allGroupBookings.stream()
                .mapToLong(b -> java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                .sum();

        double usagePercentage = totalGroupUsageMinutes > 0 
                ? (double) userUsageMinutes / totalGroupUsageMinutes 
                : 0.0;

        // Điểm ưu tiên = ownership - usage (càng cao = ưu tiên càng cao)
        return ownershipPercentage - usagePercentage;
    }

    @Transactional(readOnly = true)
    public List<Booking> listByVehicle(Long vehicleId) {
        return bookingRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getMyBookings(Long userId, String status, Long vehicleId) {
        if (status != null && vehicleId != null) {
            return bookingRepository.findByUserIdAndStatus(userId, status)
                    .stream()
                    .filter(b -> b.getVehicle().getId().equals(vehicleId))
                    .toList();
        } else if (status != null) {
            return bookingRepository.findByUserIdAndStatus(userId, status);
        } else if (vehicleId != null) {
            return bookingRepository.findByUserId(userId)
                    .stream()
                    .filter(b -> b.getVehicle().getId().equals(vehicleId))
                    .toList();
        }
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public Booking cancel(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        
        if (!booking.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Không có quyền cancel booking này");
        }
        
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể cancel booking đã bắt đầu");
        }
        
        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        
        if (!status.equals("CONFIRMED") && !status.equals("CANCELLED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Status không hợp lệ");
        }
        
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
    }
}


