package com.evcoownership.coowner.service;

import com.evcoownership.coowner.model.CommonFund;
import com.evcoownership.coowner.model.FundTransaction;
import com.evcoownership.coowner.model.Group;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.CommonFundRepository;
import com.evcoownership.coowner.repository.FundTransactionRepository;
import com.evcoownership.coowner.repository.GroupRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommonFundService {
    private final CommonFundRepository fundRepository;
    private final FundTransactionRepository transactionRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public CommonFundService(CommonFundRepository fundRepository,
                            FundTransactionRepository transactionRepository,
                            GroupRepository groupRepository,
                            UserRepository userRepository) {
        this.fundRepository = fundRepository;
        this.transactionRepository = transactionRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommonFund createFund(Long groupId, String fundType, String description) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group không tồn tại"));

        CommonFund fund = new CommonFund();
        fund.setGroup(group);
        fund.setFundType(fundType);
        fund.setBalance(BigDecimal.ZERO);
        fund.setDescription(description);
        fund.setCreatedAt(java.time.LocalDate.now());

        return fundRepository.save(fund);
    }

    @Transactional
    public FundTransaction deposit(Long fundId, BigDecimal amount, Long userId, String description) {
        CommonFund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Fund không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        fund.setBalance(fund.getBalance().add(amount));

        FundTransaction transaction = new FundTransaction();
        transaction.setFund(fund);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setCreatedBy(user);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(description);

        fundRepository.save(fund);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public FundTransaction withdraw(Long fundId, BigDecimal amount, Long userId, String description, String reference) {
        CommonFund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Fund không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        if (fund.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ");
        }

        fund.setBalance(fund.getBalance().subtract(amount));

        FundTransaction transaction = new FundTransaction();
        transaction.setFund(fund);
        transaction.setType("WITHDRAW");
        transaction.setAmount(amount);
        transaction.setCreatedBy(user);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setReference(reference);

        fundRepository.save(fund);
        return transactionRepository.save(transaction);
    }

    public List<CommonFund> getGroupFunds(Long groupId) {
        return fundRepository.findByGroupId(groupId);
    }

    public List<FundTransaction> getFundTransactions(Long fundId) {
        return transactionRepository.findByFundIdOrderByTransactionDateDesc(fundId);
    }
}

