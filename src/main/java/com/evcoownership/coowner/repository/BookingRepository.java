package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByVehicle_Id(Long vehicleId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.vehicle.id = :vehicleId " +
           "AND b.startTime < :newEndTime " +
           "AND b.endTime > :newStartTime")
    boolean existsOverlappingBooking(
        @Param("vehicleId") Long vehicleId,
        @Param("newStartTime") LocalDateTime newStartTime,
        @Param("newEndTime") LocalDateTime newEndTime
    );

    List<Booking> findByVehicle_IdAndStartTimeAfterAndEndTimeBefore(
        Long vehicleId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );

}