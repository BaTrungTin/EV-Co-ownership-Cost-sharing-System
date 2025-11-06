package com.evcoownership.coowner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.evcoownership.coowner.repository.UserRepository;
import com.evcoownership.coowner.repository.RoleRepository;
import com.evcoownership.coowner.model.Role;
import com.evcoownership.coowner.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CoOwnerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CoOwnerApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Role coOwner = roleRepository.findByName("CO_OWNER").orElseGet(() -> {
                Role r = new Role();
                r.setName("CO_OWNER");
                return roleRepository.save(r);
            });

            userRepository.findByEmail("demo@example.com").orElseGet(() -> {
                User u = new User();
                u.setEmail("demo@example.com");
                u.setFullName("Demo User");
                u.setPassword(passwordEncoder.encode("123456"));
                u.getRoles().add(coOwner);
                return userRepository.save(u);
            });
        };
    }
}


