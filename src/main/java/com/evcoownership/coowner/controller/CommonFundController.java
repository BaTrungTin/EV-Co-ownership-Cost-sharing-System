package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.CommonFund;
import com.evcoownership.coowner.model.FundTransaction;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.CommonFundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
public class CommonFundController {

    private final CommonFundService fundService;
    private final SecurityUtils securityUtils;

    public CommonFundController(CommonFundService fundService, SecurityUtils securityUtils) {
        this.fundService = fundService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<CommonFund> create(@RequestBody CreateFundRequest request) {
        // Verify user is member of the group (unless admin)
        com.evcoownership.coowner.model.User currentUser = securityUtils.getCurrentUser();
        if (!securityUtils.isAdmin(currentUser)) {
            fundService.verifyUserCanAccessGroupFunds(request.getGroupId(), currentUser.getId());
        }
        return ResponseEntity.ok(fundService.createFund(
            request.getGroupId(), 
            request.getFundType(), 
            request.getDescription()
        ));
    }

    @PostMapping("/{fundId}/deposit")
    public ResponseEntity<FundTransaction> deposit(@PathVariable Long fundId,
                                                  @RequestParam BigDecimal amount,
                                                  @RequestParam(required = false) String description) {
        User currentUser = securityUtils.getCurrentUser();
        // Verify user is member of the fund's group (unless admin)
        if (!securityUtils.isAdmin(currentUser)) {
            fundService.verifyUserCanAccessFund(fundId, currentUser.getId());
        }
        return ResponseEntity.ok(fundService.deposit(fundId, amount, currentUser.getId(), description));
    }

    @PostMapping("/{fundId}/withdraw")
    public ResponseEntity<FundTransaction> withdraw(@PathVariable Long fundId,
                                                   @RequestParam BigDecimal amount,
                                                   @RequestParam(required = false) String description,
                                                   @RequestParam(required = false) String reference) {
        User currentUser = securityUtils.getCurrentUser();
        // Verify user is member of the fund's group (unless admin)
        if (!securityUtils.isAdmin(currentUser)) {
            fundService.verifyUserCanAccessFund(fundId, currentUser.getId());
        }
        return ResponseEntity.ok(fundService.withdraw(fundId, amount, currentUser.getId(), description, reference));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CommonFund>> getGroupFunds(@PathVariable Long groupId) {
        // Verify user is member of the group (unless admin)
        com.evcoownership.coowner.model.User currentUser = securityUtils.getCurrentUser();
        if (!securityUtils.isAdmin(currentUser)) {
            fundService.verifyUserCanAccessGroupFunds(groupId, currentUser.getId());
        }
        return ResponseEntity.ok(fundService.getGroupFunds(groupId));
    }

    @GetMapping("/{fundId}/transactions")
    public ResponseEntity<List<FundTransaction>> getTransactions(@PathVariable Long fundId) {
        // Verify user is member of the fund's group (unless admin)
        com.evcoownership.coowner.model.User currentUser = securityUtils.getCurrentUser();
        if (!securityUtils.isAdmin(currentUser)) {
            fundService.verifyUserCanAccessFund(fundId, currentUser.getId());
        }
        return ResponseEntity.ok(fundService.getFundTransactions(fundId));
    }

    // Inner class for request body
    public static class CreateFundRequest {
        private Long groupId;
        private String fundType;
        private String description;

        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public String getFundType() { return fundType; }
        public void setFundType(String fundType) { this.fundType = fundType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

