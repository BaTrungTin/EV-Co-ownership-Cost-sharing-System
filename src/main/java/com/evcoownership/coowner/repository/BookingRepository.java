package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;       
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByVehicleId(Long vehicleId);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdAndStatus(Long userId, String status);
    List<Booking> findByVehicleIdAndStatus(Long vehicleId, String status);
    List<Booking> findByVehicleIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long vehicleId, LocalDateTime end, LocalDateTime start);
    List<Booking> findByVehicleIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndStatusIn(Long vehicleId, LocalDateTime end, LocalDateTime start, List<String> statuses);


    /**
     * Kiểm tra xem có booking nào (chưa bị hủy) bị CHỒNG CHÉO
     * với khoảng thời gian mới hay không.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status != 'CANCELLED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findByVehicleIdAndStartTimeAfterAndEndTimeBefore(
            Long vehicleId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}


