package com.evcoownership.coowner.controller;

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

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupFinancialReport(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
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

