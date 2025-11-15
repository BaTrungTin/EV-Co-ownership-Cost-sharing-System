package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateVehicleRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.Vehicle;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final GroupRepository groupRepository;

    public VehicleService(VehicleRepository vehicleRepository, GroupRepository groupRepository) {
        this.vehicleRepository = vehicleRepository;
        this.groupRepository = groupRepository;
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
}


