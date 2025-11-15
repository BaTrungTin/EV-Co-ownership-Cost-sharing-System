package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.Dispute;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.DisputeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private final DisputeService disputeService;
    private final SecurityUtils securityUtils;

    public DisputeController(DisputeService disputeService, SecurityUtils securityUtils) {
        this.disputeService = disputeService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<Dispute> create(@RequestBody Map<String, Object> request) {
        User currentUser = securityUtils.getCurrentUser();
        Long groupId = Long.valueOf(request.get("groupId").toString());
        return ResponseEntity.ok(disputeService.createDispute(groupId, currentUser.getId(), request));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Dispute> resolve(@PathVariable Long id,
                                         @RequestParam String resolution) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(disputeService.resolveDispute(id, currentUser.getId(), resolution));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Dispute>> getGroupDisputes(@PathVariable Long groupId) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isAdminOrStaff = currentUser.getRoles().stream()
            .anyMatch(role -> "ADMIN".equals(role.getName()) || "STAFF".equals(role.getName()));
        
        if (!isAdminOrStaff) {
        }
        return ResponseEntity.ok(disputeService.getGroupDisputes(groupId));
    }

    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<Dispute>> getOpenDisputes() {
        return ResponseEntity.ok(disputeService.getOpenDisputes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> get(@PathVariable Long id) {
        return ResponseEntity.ok(disputeService.getDispute(id));
    }
}

