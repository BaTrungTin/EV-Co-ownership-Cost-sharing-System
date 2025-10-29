package com.evcoownership.coowner.service;

import com.evcoownership.coowner.dto.CreateUserRequest;
import com.evcoownership.coowner.dto.UserDto;
import com.evcoownership.coowner.model.Role;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.RoleRepository;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public UserDto register(CreateUserRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email đã tồn tại");
        });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPassword(req.getPassword());

        Role role = roleRepository.findByName("CO_OWNER").orElseGet(() -> {
            Role r = new Role();
            r.setName("CO_OWNER");
            return roleRepository.save(r);
        });
        user.getRoles().add(role);

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}


