package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.UsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> {
    List<UsageHistory> findByUserId(Long userId);
    List<UsageHistory> findByVehicleId(Long vehicleId);
    List<UsageHistory> findByBookingId(Long bookingId);
    List<UsageHistory> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<UsageHistory> findByVehicleIdAndStartTimeBetween(Long vehicleId, LocalDateTime start, LocalDateTime end);
}

