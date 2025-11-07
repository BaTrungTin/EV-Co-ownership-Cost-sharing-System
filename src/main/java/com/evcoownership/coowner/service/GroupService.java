package com.evcoownership.coowner.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.evcoownership.coowner.controller.GroupController;
import com.evcoownership.coowner.dto.AddMemberRequest;
import com.evcoownership.coowner.dto.CreateGroupRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.OwnershipShare;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.OwnershipShareRepository;
import com.evcoownership.coowner.repository.UserRepository;

@Service
public class GroupService 
{
    private final GroupRepository groupRepository;
    private final OwnershipShareRepository ownershipShareRepository;
    private final UserRepository userRepository;

    public GroupService(
        GroupRepository groupRepository,
        OwnershipShareRepository ownershipShareRepository,
        UserRepository userRepository
    ) {
        this.groupRepository = groupRepository;
        this.ownershipShareRepository= ownershipShareRepository;
        this.userRepository = userRepository;
    }

    public Group createGroup(CreateGroupRequest request)
    {
        if (groupRepository.existsByGroupName(request.getName()))
            throw new IllegalArgumentException("Ten nhom da ton tai: " + request.getName());
            
        Group group = new Group();
        group.setGroupName(request.getName());
        group.setCreatedBy(request.getCreatedBy());
        
        return groupRepository.save(group);
    }

    public Group addMember(Long groupId, AddMemberRequest request)
    {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nhom co ID: " + groupId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung co ID: " + request.getUserId()));

        if (ownershipShareRepository.findByGroupIdAndUserId(groupId, request.getUserId()).isPresent())
            throw new IllegalArgumentException("Nguoi dung da ton tai trong nhom nay!");

        Double currentSum = ownershipShareRepository.sumPercentageByGroupId(groupId);
        if (currentSum == null)
            currentSum = 0.0;
            
        if (currentSum + request.getPercentage() > 1.0)
            throw new IllegalArgumentException("Tong ty le so huu vuot qua 100%!");

        OwnershipShare share = new OwnershipShare();
        share.setGroup(group);
        share.setUser(user);
        share.setPercentage(request.getPercentage());

        ownershipShareRepository.save(share);

        return group;
    }

    public Group getGroupDetails(Long groupId)
    {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nhom co ID: " + groupId));
    }

    public List<OwnershipShare> getGroupMembers(Long groupId)
    {
        return ownershipShareRepository.findAllByGroupId(groupId);
    }
}
