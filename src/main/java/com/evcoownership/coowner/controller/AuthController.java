package com.evcoownership.coowner.controller;

import com.evcoownership.coowner.security.JwtService;
import com.evcoownership.coowner.security.SecurityUtils;
import com.evcoownership.coowner.repository.UserRepository;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth-controller", description = "Authentication endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user with email and password, returns JWT token and user info")
    public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody LoginRequest req) {
        // findByEmail với @EntityGraph sẽ eager load roles
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        String token = jwtService.generateToken(user.getEmail());
        
        // Đảm bảo roles được load trước khi serialize (trigger loading)
        user.getRoles().size();
        
        // Trả về token và user info (bao gồm roles) để frontend có thể redirect đúng
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        
        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));
        
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Returns current authenticated user information with roles")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // getCurrentUser() sử dụng findByEmail với @EntityGraph sẽ eager load roles
        User user = securityUtils.getCurrentUser();
        
        // Đảm bảo roles được load trước khi serialize (trigger loading)
        user.getRoles().size();
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}


