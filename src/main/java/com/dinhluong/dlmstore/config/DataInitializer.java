package com.dinhluong.dlmstore.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dinhluong.dlmstore.entity.Roles;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.RoleRepository;
import com.dinhluong.dlmstore.repository.UserRepository;

@Configuration
public class DataInitializer {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {

            Roles userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Roles r = new Roles();
                        r.setName("USER");
                        return roleRepository.save(r);
                    });

            Roles adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Roles r = new Roles();
                        r.setName("ADMIN");
                        return roleRepository.save(r);
                    });

            if (!userRepository.existsByEmail("user@gmail.com")) {
                Users user = new Users();
                user.setEmail("user@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setFullName("User Demo");
                user.setRole(userRole);
                user.setAuthProvider("LOCAL");
                user.setIsEnabled(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }

            if (!userRepository.existsByEmail("admin@gmail.com")) {
                Users admin = new Users();
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Admin Demo");
                admin.setRole(adminRole);
                admin.setAuthProvider("LOCAL");
                admin.setIsEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);
            }
        };
    }

    
}

