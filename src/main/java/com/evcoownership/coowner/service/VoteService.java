package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.*;
import com.evcoownership.coowner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final UserVoteRepository userVoteRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final OwnershipShareRepository ownershipShareRepository;

    public VoteService(VoteRepository voteRepository,
                      VoteOptionRepository voteOptionRepository,
                      UserVoteRepository userVoteRepository,
                      GroupRepository groupRepository,
                      UserRepository userRepository,
                      OwnershipShareRepository ownershipShareRepository) {
        this.voteRepository = voteRepository;
        this.voteOptionRepository = voteOptionRepository;
        this.userVoteRepository = userVoteRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.ownershipShareRepository = ownershipShareRepository;
    }

    @Transactional
    public Vote createVote(Long groupId, String topic, String description, String votingMethod,
                           LocalDateTime deadline, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        
        // Check user có phải là ADMIN không - Admin không được tạo vote
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (isAdmin) {
            throw new IllegalArgumentException("Admin không thể tham gia bỏ phiếu trong nhóm");
        }
        
        // Check membership - chỉ member mới được tạo vote
        if (!ownershipShareRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("Bạn không phải là member của nhóm này");
        }

        Vote vote = new Vote();
        vote.setGroup(group);
        vote.setTopic(topic);
        vote.setDescription(description);
        vote.setVotingMethod(votingMethod);
        vote.setDeadline(deadline);
        vote.setCreatedBy(user);
        vote.setCreatedAt(LocalDateTime.now());
        vote.setStatus("OPEN");

        Vote savedVote = voteRepository.save(vote);

        // Tạo options mặc định: YES, NO
        VoteOption yesOption = new VoteOption();
        yesOption.setVote(savedVote);
        yesOption.setOption("YES");
        yesOption.setCount(0);

        VoteOption noOption = new VoteOption();
        noOption.setVote(savedVote);
        noOption.setOption("NO");
        noOption.setCount(0);

        voteOptionRepository.save(yesOption);
        voteOptionRepository.save(noOption);

        return savedVote;
    }

    @Transactional
    public UserVote castVote(Long voteId, Long userId, String choice) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote không tồn tại"));

        if (!"OPEN".equals(vote.getStatus())) {
            throw new IllegalArgumentException("Vote đã đóng");
        }

        if (vote.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Vote đã hết hạn");
        }

        if (userVoteRepository.existsByVoteIdAndUserId(voteId, userId)) {
            throw new IllegalArgumentException("Bạn đã bỏ phiếu rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        
        // Check user có phải là ADMIN không - Admin không được vote
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (isAdmin) {
            throw new IllegalArgumentException("Admin không thể tham gia bỏ phiếu trong nhóm");
        }
        
        // Check membership - chỉ member của group mới được vote
        Long groupId = vote.getGroup().getId();
        if (!ownershipShareRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("Bạn không phải là member của nhóm này");
        }

        UserVote userVote = new UserVote();
        userVote.setVote(vote);
        userVote.setUser(user);
        userVote.setChoice(choice);
        userVote.setVotedAt(LocalDateTime.now());
        userVoteRepository.save(userVote);

        // Cập nhật count cho option
        VoteOption option = voteOptionRepository.findByVoteIdAndOption(voteId, choice)
                .orElseThrow(() -> new IllegalArgumentException("Option không hợp lệ"));
        option.setCount(option.getCount() + 1);
        voteOptionRepository.save(option);

        return userVote;
    }

    public List<Vote> getGroupVotes(Long groupId) {
        return voteRepository.findByGroupId(groupId);
    }

    public Vote getVote(Long id) {
        return voteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vote không tồn tại"));
    }

    public List<VoteOption> getVoteOptions(Long voteId) {
        return voteOptionRepository.findByVoteId(voteId);
    }

    public UserVote getMyVote(Long voteId, Long userId) {
        return userVoteRepository.findByVoteIdAndUserId(voteId, userId)
                .orElse(null);
    }

    @Transactional
    public Vote closeVote(Long voteId, Long userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote không tồn tại"));

        // Chỉ người tạo vote mới được đóng
        if (!vote.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo vote mới được đóng");
        }

        if (!"OPEN".equals(vote.getStatus())) {
            throw new IllegalArgumentException("Vote đã được đóng rồi");
        }

        vote.setStatus("CLOSED");
        return voteRepository.save(vote);
    }

    @Transactional
    public void deleteVote(Long voteId, Long userId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote không tồn tại"));

        // Chỉ người tạo vote mới được xóa
        if (!vote.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo vote mới được xóa");
        }

        // Chỉ cho phép xóa nếu vote chưa đóng hoặc chưa có ai bỏ phiếu
        if (!"OPEN".equals(vote.getStatus())) {
            throw new IllegalArgumentException("Không thể xóa vote đã đóng");
        }

        long voteCount = userVoteRepository.findByVoteId(voteId).size();
        if (voteCount > 0) {
            throw new IllegalArgumentException("Không thể xóa vote đã có người bỏ phiếu");
        }

        // Xóa vote options và user votes (cascade delete sẽ tự động xóa)
        voteOptionRepository.deleteAll(voteOptionRepository.findByVoteId(voteId));
        userVoteRepository.deleteAll(userVoteRepository.findByVoteId(voteId));
        
        // Xóa vote
        voteRepository.delete(vote);
    }
}

