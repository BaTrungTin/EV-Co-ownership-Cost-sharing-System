package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateEContractRequest;
import com.evcoownership.coowner.model.EContract;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.EContractRepository;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EContractService {
    private final EContractRepository contractRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public EContractService(EContractRepository contractRepository,
                            GroupRepository groupRepository,
                            UserRepository userRepository) {
        this.contractRepository = contractRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EContract createContract(CreateEContractRequest req, Long userId) {
        if (contractRepository.findByContractNo(req.getContractNo()).isPresent()) {
            throw new IllegalArgumentException("Contract number đã tồn tại");
        }

        Group group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        EContract contract = new EContract();
        contract.setGroup(group);
        contract.setContractNo(req.getContractNo());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());
        contract.setTerms(req.getTerms());
        contract.setDocumentUrl(req.getDocumentUrl());
        contract.setStatus("DRAFT");
        contract.setCreatedBy(user);
        contract.setCreatedAt(LocalDate.now());

        EContract savedContract = contractRepository.save(contract);
        
        // Force initialize all lazy-loaded associations to avoid proxy serialization issues
        try {
            Hibernate.initialize(savedContract.getGroup());
            if (savedContract.getGroup() != null) {
                Group savedGroup = savedContract.getGroup();
                savedGroup.getId();
                savedGroup.getName();
                // Force initialize group.createdBy
                Hibernate.initialize(savedGroup.getCreatedBy());
                if (savedGroup.getCreatedBy() != null) {
                    User groupCreator = savedGroup.getCreatedBy();
                    groupCreator.getId();
                    groupCreator.getEmail();
                    groupCreator.getFullName();
                }
            }
            Hibernate.initialize(savedContract.getCreatedBy());
            if (savedContract.getCreatedBy() != null) {
                User creator = savedContract.getCreatedBy();
                creator.getId();
                creator.getEmail();
                creator.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error loading saved contract " + savedContract.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedContract;
    }

    @Transactional
    public EContract signContract(Long contractId) {
        EContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract không tồn tại"));
        contract.setStatus("SIGNED");
        contract.setSignedAt(LocalDate.now());
        
        EContract savedContract = contractRepository.save(contract);
        
        // Force initialize all lazy-loaded associations to avoid proxy serialization issues
        try {
            Hibernate.initialize(savedContract.getGroup());
            if (savedContract.getGroup() != null) {
                Group group = savedContract.getGroup();
                group.getId();
                group.getName();
                // Force initialize group.createdBy
                Hibernate.initialize(group.getCreatedBy());
                if (group.getCreatedBy() != null) {
                    User groupCreator = group.getCreatedBy();
                    groupCreator.getId();
                    groupCreator.getEmail();
                    groupCreator.getFullName();
                }
            }
            Hibernate.initialize(savedContract.getCreatedBy());
            if (savedContract.getCreatedBy() != null) {
                User creator = savedContract.getCreatedBy();
                creator.getId();
                creator.getEmail();
                creator.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error loading signed contract " + savedContract.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedContract;
    }

    @Transactional(readOnly = true)
    public List<EContract> getGroupContracts(Long groupId) {
        // Verify group exists first
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group không tồn tại với ID: " + groupId);
        }
        
        List<EContract> contracts = contractRepository.findByGroupId(groupId);
        // Force initialize all lazy-loaded associations to avoid proxy serialization issues
        for (EContract contract : contracts) {
            try {
                // Force initialize group
                Hibernate.initialize(contract.getGroup());
                if (contract.getGroup() != null) {
                    Group group = contract.getGroup();
                    group.getId();
                    group.getName();
                    // Force initialize group.createdBy
                    Hibernate.initialize(group.getCreatedBy());
                    if (group.getCreatedBy() != null) {
                        User groupCreator = group.getCreatedBy();
                        groupCreator.getId();
                        groupCreator.getEmail();
                        groupCreator.getFullName();
                    }
                }
                // Force initialize createdBy
                Hibernate.initialize(contract.getCreatedBy());
                if (contract.getCreatedBy() != null) {
                    User creator = contract.getCreatedBy();
                    creator.getId();
                    creator.getEmail();
                    creator.getFullName();
                }
            } catch (Exception e) {
                System.err.println("Error loading contract " + contract.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return contracts;
    }

    @Transactional(readOnly = true)
    public EContract getContract(Long id) {
        EContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract không tồn tại"));
        // Force initialize all lazy-loaded associations
        try {
            Hibernate.initialize(contract.getGroup());
            if (contract.getGroup() != null) {
                Group group = contract.getGroup();
                group.getId();
                group.getName();
                // Force initialize group.createdBy
                Hibernate.initialize(group.getCreatedBy());
                if (group.getCreatedBy() != null) {
                    User groupCreator = group.getCreatedBy();
                    groupCreator.getId();
                    groupCreator.getEmail();
                    groupCreator.getFullName();
                }
            }
            Hibernate.initialize(contract.getCreatedBy());
            if (contract.getCreatedBy() != null) {
                User creator = contract.getCreatedBy();
                creator.getId();
                creator.getEmail();
                creator.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error loading contract " + contract.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return contract;
    }
}





