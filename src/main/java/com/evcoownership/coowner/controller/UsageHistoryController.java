package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.UsageHistory;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.UsageHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage-history")
public class UsageHistoryController {

    private final UsageHistoryService usageHistoryService;
    private final SecurityUtils securityUtils;

    public UsageHistoryController(UsageHistoryService usageHistoryService, SecurityUtils securityUtils) {
        this.usageHistoryService = usageHistoryService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<UsageHistory> checkIn(@RequestParam Long bookingId,
                                                @RequestParam Integer odometer) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(usageHistoryService.checkIn(bookingId, currentUser.getId(), odometer));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<UsageHistory> checkOut(@PathVariable Long id,
                                                  @RequestParam Integer odometer,
                                                  @RequestParam(required = false) String notes) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(usageHistoryService.checkOut(id, currentUser.getId(), odometer, notes));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UsageHistory>> getUserHistory(@PathVariable Long userId) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isAdminOrStaff = currentUser.getRoles().stream()
            .anyMatch(role -> "ADMIN".equals(role.getName()) || "STAFF".equals(role.getName()));
        
        if (!isAdminOrStaff && !currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem lịch sử của user khác");
        }
        return ResponseEntity.ok(usageHistoryService.getUserHistory(userId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<UsageHistory>> getVehicleHistory(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(usageHistoryService.getVehicleHistory(vehicleId));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<UsageHistory>> getMyHistory() {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(usageHistoryService.getUserHistory(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsageHistory> get(@PathVariable Long id) {
        return ResponseEntity.ok(usageHistoryService.getHistory(id));
    }
}

