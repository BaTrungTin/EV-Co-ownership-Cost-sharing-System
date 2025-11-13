package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.Booking;
import com.evcoownership.coowner.model.UsageHistory;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.BookingRepository;
import com.evcoownership.coowner.repository.UsageHistoryRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsageHistoryService {
    private final UsageHistoryRepository usageHistoryRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public UsageHistoryService(UsageHistoryRepository usageHistoryRepository,
                              BookingRepository bookingRepository,
                              UserRepository userRepository) {
        this.usageHistoryRepository = usageHistoryRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UsageHistory checkIn(Long bookingId, Long staffUserId, Integer odometer) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking chưa được confirm");
        }

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user không tồn tại"));

        UsageHistory history = new UsageHistory();
        history.setBooking(booking);
        history.setVehicle(booking.getVehicle());
        history.setUser(booking.getUser());
        history.setStartTime(booking.getStartTime());
        history.setEndTime(booking.getEndTime());
        history.setStartOdometer(odometer);
        history.setCheckedInBy(staff);
        history.setCheckedInAt(LocalDateTime.now());
        history.setCheckInQrCode(UUID.randomUUID().toString());

        return usageHistoryRepository.save(history);
    }

    @Transactional
    public UsageHistory checkOut(Long usageHistoryId, Long staffUserId, Integer odometer, String notes) {
        UsageHistory history = usageHistoryRepository.findById(usageHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("Usage history không tồn tại"));

        if (history.getCheckedOutAt() != null) {
            throw new IllegalArgumentException("Đã check-out rồi");
        }

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user không tồn tại"));

        history.setEndOdometer(odometer);
        if (history.getStartOdometer() != null && odometer != null) {
            history.setDistance(odometer - history.getStartOdometer());
        }
        history.setNotes(notes);
        history.setCheckedOutBy(staff);
        history.setCheckedOutAt(LocalDateTime.now());
        history.setCheckOutQrCode(UUID.randomUUID().toString());

        return usageHistoryRepository.save(history);
    }

    public List<UsageHistory> getUserHistory(Long userId) {
        return usageHistoryRepository.findByUserId(userId);
    }

    public List<UsageHistory> getVehicleHistory(Long vehicleId) {
        return usageHistoryRepository.findByVehicleId(vehicleId);
    }

    public UsageHistory getHistory(Long id) {
        return usageHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usage history không tồn tại"));
    }
}

