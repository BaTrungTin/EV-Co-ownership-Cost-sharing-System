package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.Dispute;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.DisputeRepository;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DisputeService {
    private final DisputeRepository disputeRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public DisputeService(DisputeRepository disputeRepository,
                         GroupRepository groupRepository,
                         UserRepository userRepository) {
        this.disputeRepository = disputeRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Dispute createDispute(Long groupId, Long userId, Map<String, Object> request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        Dispute dispute = new Dispute();
        dispute.setGroup(group);
        dispute.setCreatedBy(user);
        dispute.setTitle(request.get("title").toString());
        dispute.setDescription(request.get("description").toString());
        dispute.setCategory(request.getOrDefault("category", "OTHER").toString());
        dispute.setStatus("OPEN");
        dispute.setCreatedAt(LocalDateTime.now());

        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute resolveDispute(Long disputeId, Long staffUserId, String resolution) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute không tồn tại"));
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user không tồn tại"));

        dispute.setStatus("RESOLVED");
        dispute.setResolution(resolution);
        dispute.setResolvedBy(staff);
        dispute.setResolvedAt(LocalDateTime.now());

        return disputeRepository.save(dispute);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public List<Dispute> getGroupDisputes(Long groupId) {
        return disputeRepository.findByGroupId(groupId);
    }

    public List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatus("OPEN");
    }

    public Dispute getDispute(Long id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute không tồn tại"));
    }
}

