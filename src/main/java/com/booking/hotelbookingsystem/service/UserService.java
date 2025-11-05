package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.UserDto;
import com.booking.hotelbookingsystem.model.User;

public interface UserService {
    void saveUser(UserDto userDto);
    User findByEmail(String email);
    
    // --- ADD THIS METHOD ---
    long countUsers();
}