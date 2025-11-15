package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateVehicleRequest;
import com.evcoownership.coowner.model.Vehicle;
import com.evcoownership.coowner.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@Validated @RequestBody CreateVehicleRequest req) {
        return ResponseEntity.ok(vehicleService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> list(@RequestParam(required = false) Long groupId) {
        if (groupId != null) {
            return ResponseEntity.ok(vehicleService.listByGroup(groupId));
        }
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> get(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}



