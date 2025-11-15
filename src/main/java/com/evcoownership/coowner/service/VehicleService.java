package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateVehicleRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.Vehicle;
import com.evcoownership.coowner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final GroupRepository groupRepository;
    private final BookingRepository bookingRepository;
    private final ExpenseRepository expenseRepository;

    public VehicleService(VehicleRepository vehicleRepository, 
                         GroupRepository groupRepository,
                         BookingRepository bookingRepository,
                         ExpenseRepository expenseRepository) {
        this.vehicleRepository = vehicleRepository;
        this.groupRepository = groupRepository;
        this.bookingRepository = bookingRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Vehicle create(CreateVehicleRequest req) {
        vehicleRepository.findByVin(req.getVin()).ifPresent(v -> { throw new IllegalArgumentException("VIN đã tồn tại"); });
        vehicleRepository.findByPlate(req.getPlate()).ifPresent(v -> { throw new IllegalArgumentException("Biển số đã tồn tại"); });
        Group g = groupRepository.findById(req.getGroupId()).orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        Vehicle v = new Vehicle();
        v.setVin(req.getVin());
        v.setPlate(req.getPlate());
        v.setModel(req.getModel());
        v.setGroup(g);
        return vehicleRepository.save(v);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listByGroup(Long groupId) {
        return vehicleRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle không tồn tại"));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle không tồn tại"));

        // Kiểm tra các entities liên quan
        List<String> errors = new java.util.ArrayList<>();

        long bookingCount = bookingRepository.findByVehicleId(id).size();
        if (bookingCount > 0) {
            errors.add("Xe có " + bookingCount + " booking. Vui lòng xóa booking trước.");
        }

        long expenseCount = expenseRepository.findByVehicleId(id).size();
        if (expenseCount > 0) {
            errors.add("Xe có " + expenseCount + " chi phí. Không thể xóa xe có chi phí.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        vehicleRepository.delete(vehicle);
    }
}


