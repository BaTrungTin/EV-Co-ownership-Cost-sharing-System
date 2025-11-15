package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.AddMemberRequest;
import com.evcoownership.coowner.dto.CreateGroupRequest;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.OwnershipShare;
import com.evcoownership.coowner.model.User;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final OwnershipShareRepository ownershipShareRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ExpenseRepository expenseRepository;
    private final BookingRepository bookingRepository;
    private final VoteRepository voteRepository;
    private final CommonFundRepository commonFundRepository;

    public GroupService(GroupRepository groupRepository,
                        OwnershipShareRepository ownershipShareRepository,
                        UserRepository userRepository,
                        VehicleRepository vehicleRepository,
                        ExpenseRepository expenseRepository,
                        BookingRepository bookingRepository,
                        VoteRepository voteRepository,
                        CommonFundRepository commonFundRepository) {
        this.groupRepository = groupRepository;
        this.ownershipShareRepository = ownershipShareRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.expenseRepository = expenseRepository;
        this.bookingRepository = bookingRepository;
        this.voteRepository = voteRepository;
        this.commonFundRepository = commonFundRepository;
    }

    @Transactional
    public Group createGroup(CreateGroupRequest req, Long userId) {
        groupRepository.findByName(req.getName()).ifPresent(g -> {
            throw new IllegalArgumentException("Group name đã tồn tại");
        });
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Group g = new Group();
        g.setName(req.getName());
        g.setCreatedBy(creator);
        return groupRepository.save(g);
    }

    @Transactional
    public OwnershipShare addMember(Long groupId, AddMemberRequest req) {
        Group g = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        User u = userRepository.findById(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        if (ownershipShareRepository.existsByGroupIdAndUserId(groupId, u.getId())) {
            throw new IllegalArgumentException("User đã ở trong nhóm");
        }
        double currentSum = ownershipShareRepository.findByGroupId(groupId)
                .stream().mapToDouble(OwnershipShare::getPercentage).sum();
        if (currentSum + req.getPercentage() > 1.0 + 1e-9) {
            throw new IllegalArgumentException("Tổng percentage vượt quá 1.0");
        }
        OwnershipShare share = new OwnershipShare();
        share.setGroup(g);
        share.setUser(u);
        share.setPercentage(req.getPercentage());
        return ownershipShareRepository.save(share);
    }

    @Transactional(readOnly = true)
    public Group getGroup(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        // Trigger load ownershipShares để tránh LazyInitializationException
        group.getOwnershipShares().size();
        return group;
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        // Sử dụng findAllWithMembers để đảm bảo load đầy đủ ownershipShares và user
        // Nếu có lỗi, fallback về findAll()
        List<Group> groups;
        try {
            groups = groupRepository.findAllWithMembers();
        } catch (Exception e) {
            // Fallback về findAll() nếu query có vấn đề
            groups = groupRepository.findAll();
        }
        // Trigger load ownershipShares và user trong ownershipShares cho tất cả groups để tránh LazyInitializationException
        // Force initialize all lazy-loaded associations to avoid proxy serialization issues
        for (Group group : groups) {
            try {
                // Force initialize ownershipShares collection using Hibernate.initialize()
                Hibernate.initialize(group.getOwnershipShares());
                if (group.getOwnershipShares() != null) {
                    // Force initialize each ownershipShare and its user
                    for (OwnershipShare share : group.getOwnershipShares()) {
                        // Force initialize user using Hibernate.initialize()
                        Hibernate.initialize(share.getUser());
                        if (share.getUser() != null) {
                            User user = share.getUser();
                            // Access all fields to ensure they're loaded
                            user.getId();
                            user.getEmail();
                            user.getFullName();
                        }
                        // Force initialize group in share (should already be loaded)
                        Hibernate.initialize(share.getGroup());
                    }
                }
                // Force initialize createdBy using Hibernate.initialize()
                Hibernate.initialize(group.getCreatedBy());
                if (group.getCreatedBy() != null) {
                    User creator = group.getCreatedBy();
                    // Access all fields to ensure they're loaded
                    creator.getId();
                    creator.getEmail();
                    creator.getFullName();
                }
            } catch (Exception e) {
                // Log but continue with other groups
                System.err.println("Error loading group " + group.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return groups;
    }

    @Transactional(readOnly = true)
    public List<Group> getUserGroups(Long userId) {
        // Trả về các groups mà user là member HOẶC user là creator
        java.util.Set<Long> groupIds = new java.util.HashSet<>();
        List<Group> groups = new java.util.ArrayList<>();
        
        // Lấy groups mà user là member (có ownership share)
        List<OwnershipShare> shares = ownershipShareRepository.findByUserId(userId);
        for (OwnershipShare share : shares) {
            Long groupId = share.getGroup().getId();
            if (!groupIds.contains(groupId)) {
                // Reload group với EntityGraph để đảm bảo ownershipShares được load
                Group group = groupRepository.findById(groupId)
                        .orElse(null);
                if (group != null) {
                    // Trigger load ownershipShares để tránh LazyInitializationException
                    group.getOwnershipShares().size();
                    groups.add(group);
                    groupIds.add(groupId);
                }
            }
        }
        
        // Lấy groups mà user là creator (người tạo group)
        List<Group> createdGroups = groupRepository.findByCreatedById(userId);
        // Thêm các groups mà user tạo nhưng chưa có trong list (tránh duplicate)
        for (Group createdGroup : createdGroups) {
            if (!groupIds.contains(createdGroup.getId())) {
                // Trigger load ownershipShares để tránh LazyInitializationException
                createdGroup.getOwnershipShares().size();
                groups.add(createdGroup);
                groupIds.add(createdGroup.getId());
            }
        }
        
        return groups;
    }

    @Transactional(readOnly = true)
    public void verifyUserIsMember(Long groupId, Long userId) {
        // Check nếu user là member (có ownership share)
        if (ownershipShareRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return;
        }
        
        // Check nếu user là creator của group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        if (group.getCreatedBy() != null && group.getCreatedBy().getId().equals(userId)) {
            return;
        }
        
        throw new IllegalArgumentException("Bạn không phải là member của nhóm này");
    }

    @Transactional(readOnly = true)
    public void verifyUserIsGroupOwner(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        if (group.getCreatedBy() == null || !group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo nhóm mới có quyền thực hiện thao tác này");
        }
    }

    @Transactional
    public Group transferOwnership(Long groupId, Long newOwnerId, Long currentOwnerId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        
        // Verify current user is the owner
        if (group.getCreatedBy() == null || !group.getCreatedBy().getId().equals(currentOwnerId)) {
            throw new IllegalArgumentException("Chỉ người tạo nhóm mới có quyền chuyển quyền trưởng nhóm");
        }
        
        // Verify new owner exists
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        
        // Verify new owner is a member of the group
        if (!ownershipShareRepository.existsByGroupIdAndUserId(groupId, newOwnerId)) {
            throw new IllegalArgumentException("Người dùng phải là thành viên của nhóm trước khi được chuyển quyền trưởng nhóm");
        }
        
        // Transfer ownership
        group.setCreatedBy(newOwner);
        return groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));

        // Kiểm tra các entities liên quan
        List<String> errors = new java.util.ArrayList<>();

        long vehicleCount = vehicleRepository.findByGroupId(id).size();
        if (vehicleCount > 0) {
            errors.add("Nhóm có " + vehicleCount + " xe. Vui lòng xóa xe trước.");
        }

        long expenseCount = expenseRepository.findByGroupId(id).size();
        if (expenseCount > 0) {
            errors.add("Nhóm có " + expenseCount + " chi phí. Không thể xóa nhóm có chi phí.");
        }

        long bookingCount = bookingRepository.findByGroupId(id).size();
        if (bookingCount > 0) {
            errors.add("Nhóm có " + bookingCount + " booking. Không thể xóa nhóm có booking.");
        }

        long voteCount = voteRepository.findByGroupId(id).size();
        if (voteCount > 0) {
            errors.add("Nhóm có " + voteCount + " cuộc bỏ phiếu. Không thể xóa nhóm có vote.");
        }

        long fundCount = commonFundRepository.findByGroupId(id).size();
        if (fundCount > 0) {
            errors.add("Nhóm có " + fundCount + " quỹ chung. Vui lòng xóa quỹ trước.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        // Xóa ownershipShares (cascade delete tự động xóa)
        // Xóa group
        groupRepository.delete(group);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        // Validate group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        
        OwnershipShare share = ownershipShareRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User không ở trong nhóm này"));

        // Kiểm tra xem user có booking đang active không
        List<String> errors = new java.util.ArrayList<>();
        
        long activeBookingCount = bookingRepository.findByUserIdAndStatus(userId, "CONFIRMED").size() +
                                 bookingRepository.findByUserIdAndStatus(userId, "PENDING").size();
        if (activeBookingCount > 0) {
            errors.add("User có " + activeBookingCount + " booking đang active. Vui lòng hủy booking trước.");
        }

        // Kiểm tra xem user có expense share chưa thanh toán không
        // (có thể bỏ qua nếu muốn cho phép remove member và giữ lại debt)
        // long unpaidExpenseCount = expenseShareRepository.findByUserId(userId)
        //     .stream().filter(s -> "PENDING".equals(s.getStatus())).count();
        // if (unpaidExpenseCount > 0) {
        //     errors.add("User có " + unpaidExpenseCount + " chi phí chưa thanh toán.");
        // }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        ownershipShareRepository.delete(share);
    }

    @Transactional(readOnly = true)
    public List<OwnershipShare> getGroupMembers(Long groupId) {
        List<OwnershipShare> shares = ownershipShareRepository.findByGroupId(groupId);
        // Ensure user and group are loaded (EntityGraph should handle this, but trigger to be safe)
        for (OwnershipShare share : shares) {
            if (share.getUser() != null) {
                share.getUser().getId(); // Trigger load
            }
            if (share.getGroup() != null) {
                share.getGroup().getId(); // Trigger load
            }
        }
        return shares;
    }
}


