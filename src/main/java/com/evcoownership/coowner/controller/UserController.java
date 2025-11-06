package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateUserRequest;
import com.evcoownership.coowner.dto.UserDto;
import com.evcoownership.coowner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "user-controller", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return userService.listUsers(pageable);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Validated @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }
}


