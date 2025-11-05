package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.UserDto;
import com.booking.hotelbookingsystem.model.Role;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.RoleRepository;
import com.booking.hotelbookingsystem.repository.UserRepository;
import com.booking.hotelbookingsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
        }
        user.setRoles(Set.of(userRole));
        
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // --- ADD THIS METHOD ---
    @Override
    public long countUsers() {
        return userRepository.count();
    }
}