package com.evcoownership.coowner.repository;

import com.evcoownership.coowner.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByPlate(String plate);
    List<Vehicle> findByGroup_Id(Long groupId);
}
