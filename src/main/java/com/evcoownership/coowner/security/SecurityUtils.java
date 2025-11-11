package com.evcoownership.coowner.security;

import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    
    private final UserRepository userRepository;
    
    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("User chưa đăng nhập");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
    }
    
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalArgumentException("User chưa đăng nhập");
        }
        return auth.getName();
    }
}

