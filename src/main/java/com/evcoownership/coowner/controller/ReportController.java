package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    public ReportController(ReportService reportService, SecurityUtils securityUtils) {
        this.reportService = reportService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupFinancialReport(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Verify user is member of the group (unless admin)
        com.evcoownership.coowner.model.User currentUser = securityUtils.getCurrentUser();
        if (!securityUtils.isAdmin(currentUser)) {
            // Only verify membership if user is not admin
            reportService.verifyUserCanAccessGroupReport(groupId, currentUser.getId());
        }
        return ResponseEntity.ok(reportService.getGroupFinancialReport(groupId, startDate, endDate));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserExpenseReport(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getUserExpenseReport(userId, startDate, endDate));
    }

    @GetMapping("/usage-vs-ownership/{groupId}")
    public ResponseEntity<Map<String, Object>> compareUsageVsOwnership(@PathVariable Long groupId) {
        return ResponseEntity.ok(reportService.compareUsageVsOwnership(groupId));
    }
}

