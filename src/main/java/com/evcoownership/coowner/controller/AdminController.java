package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.model.*;
import com.evcoownership.coowner.repository.*;
import com.evcoownership.coowner.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final GroupService groupService;

    public AdminController(GroupRepository groupRepository,
                          UserRepository userRepository,
                          VehicleRepository vehicleRepository,
                          BookingRepository bookingRepository,
                          ExpenseRepository expenseRepository,
                          PaymentRepository paymentRepository,
                          GroupService groupService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getAllGroups() {
        // Use GroupService.getAllGroups() to ensure ownershipShares are loaded
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Sử dụng findAllWithRoles() để đảm bảo load roles
        // Nếu có lỗi, fallback về findAll() (có EntityGraph)
        try {
            List<User> users = userRepository.findAllWithRoles();
            // Trigger loading của roles cho mỗi user để đảm bảo được load
            users.forEach(user -> user.getRoles().size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Fallback về findAll() nếu query có vấn đề
            List<User> users = userRepository.findAll();
            // Trigger loading của roles cho mỗi user
            users.forEach(user -> user.getRoles().size());
            return ResponseEntity.ok(users);
        }
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @GetMapping("/expenses")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        // Force initialize all lazy-loaded associations to avoid proxy serialization issues
        for (Expense expense : expenses) {
            try {
                // Force initialize group and its associations
                if (expense.getGroup() != null) {
                    org.hibernate.Hibernate.initialize(expense.getGroup());
                    // Access group fields to ensure they're loaded
                    expense.getGroup().getId();
                    expense.getGroup().getName();
                    // Force initialize group.createdBy
                    if (expense.getGroup().getCreatedBy() != null) {
                        org.hibernate.Hibernate.initialize(expense.getGroup().getCreatedBy());
                        expense.getGroup().getCreatedBy().getId();
                        expense.getGroup().getCreatedBy().getEmail();
                        expense.getGroup().getCreatedBy().getFullName();
                    }
                }
                // Force initialize vehicle
                if (expense.getVehicle() != null) {
                    org.hibernate.Hibernate.initialize(expense.getVehicle());
                    expense.getVehicle().getId();
                    expense.getVehicle().getPlate();
                }
                // Force initialize createdBy
                if (expense.getCreatedBy() != null) {
                    org.hibernate.Hibernate.initialize(expense.getCreatedBy());
                    expense.getCreatedBy().getId();
                    expense.getCreatedBy().getEmail();
                    expense.getCreatedBy().getFullName();
                }
            } catch (Exception e) {
                // Log but continue with other expenses
                System.err.println("Error loading expense " + expense.getId() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}

