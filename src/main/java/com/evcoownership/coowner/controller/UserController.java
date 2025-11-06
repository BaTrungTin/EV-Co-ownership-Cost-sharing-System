package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.dto.CreateUserRequest;
import com.evcoownership.coowner.dto.UserDto;
import com.evcoownership.coowner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> list() {
        return userService.listUsers();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Validated @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }
}


