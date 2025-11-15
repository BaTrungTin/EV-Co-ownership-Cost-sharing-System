package com.evcoownership.coowner.security;

import com.evcoownership.coowner.repository.UserRepository;
import com.evcoownership.coowner.model.User;
import com.evcoownership.coowner.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Validate token (throws exception if invalid)
            jwtService.validateToken(token);
            
            // Extract email from token (throws exception if invalid)
            String email = jwtService.extractEmail(token);
            if (email == null || email.isEmpty()) {
                logger.warn("JWT token does not contain email");
                filterChain.doFilter(request, response);
                return;
            }
            
            // Find user by email
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                logger.warn("User not found for email: " + email);
                filterChain.doFilter(request, response);
                return;
            }
            
            // Set authentication if not already set
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user roles and convert to authorities
                Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList());
                
                // Nếu user không có roles, thêm role mặc định để đảm bảo authentication hoạt động
                if (authorities.isEmpty()) {
                    logger.warn("User " + email + " has no roles, adding default CO_OWNER role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_CO_OWNER"));
                }
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        user.getEmail(), 
                        null, 
                        authorities
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set for user: " + email + " with roles: " + 
                    user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
            }
        } catch (Exception e) {
            // Log the exception for debugging
            logger.error("JWT authentication failed: " + e.getMessage(), e);
            // Clear any existing authentication to prevent security issues
            SecurityContextHolder.clearContext();
            // Continue without authentication - will result in 401/403 if endpoint requires auth
        }
        
        filterChain.doFilter(request, response);
    }
}

