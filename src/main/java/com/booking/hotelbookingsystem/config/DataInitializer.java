package com.booking.hotelbookingsystem.config;

import com.booking.hotelbookingsystem.model.Role;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.RoleRepository;
import com.booking.hotelbookingsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // --- 1. Create Roles ---
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = roleRepository.save(new Role("ROLE_USER"));
        }
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // --- 2. Create Admin User ---
        // Check if the admin user already exists
        if (userRepository.findByEmail("admin@test.com") == null) {
            User adminUser = new User();
            adminUser.setName("Admin User");
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword(passwordEncoder.encode("admin123")); // Set default password
            
            // Assign both USER and ADMIN roles
            adminUser.setRoles(Set.of(userRole, adminRole)); 
            
            userRepository.save(adminUser);
        }
    }
}