package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateEContractRequest;
import com.evcoownership.coowner.model.EContract;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.EContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class EContractController {

    private final EContractService contractService;
    private final SecurityUtils securityUtils;

    public EContractController(EContractService contractService, SecurityUtils securityUtils) {
        this.contractService = contractService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<EContract> create(@Validated @RequestBody CreateEContractRequest req) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(contractService.createContract(req, currentUser.getId()));
    }

    @PutMapping("/{id}/sign")
    public ResponseEntity<EContract> sign(@PathVariable Long id) {
        // User chỉ ký được contract của group mình thuộc về
        User currentUser = securityUtils.getCurrentUser();
        // TODO: Check if user is member of contract's group
        return ResponseEntity.ok(contractService.signContract(id));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EContract>> getGroupContracts(@PathVariable Long groupId) {
        // User chỉ xem được contracts của group mình thuộc về
        User currentUser = securityUtils.getCurrentUser();
        boolean isAdminOrStaff = currentUser.getRoles().stream()
            .anyMatch(role -> "ADMIN".equals(role.getName()) || "STAFF".equals(role.getName()));
        
        if (!isAdminOrStaff) {
            // TODO: Check if user is member of group
        }
        return ResponseEntity.ok(contractService.getGroupContracts(groupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EContract> get(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContract(id));
    }
}





