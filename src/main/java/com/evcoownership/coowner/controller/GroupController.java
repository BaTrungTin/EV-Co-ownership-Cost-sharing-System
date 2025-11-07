package com.evcoownership.coowner.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evcoownership.coowner.dto.AddMemberRequest;
import com.evcoownership.coowner.dto.CreateGroupRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.OwnershipShare;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.OwnershipShareRepository;
import com.evcoownership.coowner.repository.UserRepository;
import com.evcoownership.coowner.service.GroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class GroupController 
{
    private final GroupService groupService;

    public GroupController(GroupService groupService) 
    {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody @Valid CreateGroupRequest request) 
    {
        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Group> addMember(@PathVariable Long id, @RequestBody @Valid AddMemberRequest request) 
    {
        return ResponseEntity.ok(groupService.addMember(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupDetails(@PathVariable Long id) 
    {
        Group group = groupService.getGroupDetails(id);
        List<OwnershipShare> members = groupService.getGroupMembers(id);
        return ResponseEntity.ok(Map.of("group", group, "members", members));
    }
}