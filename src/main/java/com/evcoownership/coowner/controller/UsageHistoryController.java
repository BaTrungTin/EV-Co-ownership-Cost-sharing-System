package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.UsageHistory;
import com.evcoownership.coowner.service.UsageHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage-history")
public class UsageHistoryController {

    private final UsageHistoryService usageHistoryService;

    public UsageHistoryController(UsageHistoryService usageHistoryService) {
        this.usageHistoryService = usageHistoryService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<UsageHistory> checkIn(@RequestParam Long bookingId,
                                                @RequestParam Long staffUserId,
                                                @RequestParam Integer odometer) {
        return ResponseEntity.ok(usageHistoryService.checkIn(bookingId, staffUserId, odometer));
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<UsageHistory> checkOut(@PathVariable Long id,
                                                  @RequestParam Long staffUserId,
                                                  @RequestParam Integer odometer,
                                                  @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(usageHistoryService.checkOut(id, staffUserId, odometer, notes));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UsageHistory>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(usageHistoryService.getUserHistory(userId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<UsageHistory>> getVehicleHistory(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(usageHistoryService.getVehicleHistory(vehicleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsageHistory> get(@PathVariable Long id) {
        return ResponseEntity.ok(usageHistoryService.getHistory(id));
    }
}

