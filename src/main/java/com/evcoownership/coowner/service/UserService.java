package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateUserRequest;
import com.evcoownership.coowner.dto.UserDto;
import com.evcoownership.coowner.model.Role;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.RoleRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto register(CreateUserRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email đã tồn tại");
        });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        Role role = roleRepository.findByName("CO_OWNER").orElseGet(() -> {
            Role r = new Role();
            r.setName("CO_OWNER");
            return roleRepository.save(r);
        });
        user.getRoles().add(role);

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(Pageable pageable) {
        // Sử dụng findAllWithRoles() nếu cần, nhưng với pagination thì dùng EntityGraph
        // EntityGraph trong repository sẽ tự động load roles
        return userRepository.findAll(pageable).map(this::toDto);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}


