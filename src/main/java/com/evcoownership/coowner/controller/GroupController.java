package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.AddMemberRequest;
import com.evcoownership.coowner.dto.CreateGroupRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.OwnershipShare;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final SecurityUtils securityUtils;

    public GroupController(GroupService groupService, SecurityUtils securityUtils) {
        this.groupService = groupService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<Group>> list() {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(groupService.getUserGroups(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<Group> create(@Validated @RequestBody CreateGroupRequest req) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(groupService.createGroup(req, currentUser.getId()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<OwnershipShare> addMember(@PathVariable Long id,
                                                    @Validated @RequestBody AddMemberRequest req) {
        User currentUser = securityUtils.getCurrentUser();
        // Chỉ người tạo group (group owner) mới được thêm member
        groupService.verifyUserIsGroupOwner(id, currentUser.getId());
        return ResponseEntity.ok(groupService.addMember(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> get(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin có thể xem tất cả groups, user thường chỉ xem được groups mình là member
        if (!securityUtils.isAdmin(currentUser)) {
            groupService.verifyUserIsMember(id, currentUser.getId());
        }
        return ResponseEntity.ok(groupService.getGroup(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Chỉ người tạo group (group owner) mới được xóa group
        groupService.verifyUserIsGroupOwner(id, currentUser.getId());
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<OwnershipShare>> getMembers(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        // Admin có thể xem members của tất cả groups, user thường chỉ xem được groups mình là member
        if (!securityUtils.isAdmin(currentUser)) {
            groupService.verifyUserIsMember(id, currentUser.getId());
        }
        return ResponseEntity.ok(groupService.getGroupMembers(id));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = securityUtils.getCurrentUser();
        // Chỉ người tạo group (group owner) mới được remove member
        groupService.verifyUserIsGroupOwner(groupId, currentUser.getId());
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{groupId}/transfer-ownership")
    public ResponseEntity<Group> transferOwnership(@PathVariable Long groupId,
                                                   @RequestBody TransferOwnershipRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(groupService.transferOwnership(groupId, request.getNewOwnerId(), currentUser.getId()));
    }

    // Inner class for request body
    public static class TransferOwnershipRequest {
        private Long newOwnerId;

        public Long getNewOwnerId() {
            return newOwnerId;
        }

        public void setNewOwnerId(Long newOwnerId) {
            this.newOwnerId = newOwnerId;
        }
    }
}


