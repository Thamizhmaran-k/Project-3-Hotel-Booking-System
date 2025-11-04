package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.UserDto;
import com.booking.hotelbookingsystem.model.Role;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.RoleRepository;
import com.booking.hotelbookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(null, "Test User", "test@example.com", "plainPassword");
        userRole = new Role("ROLE_USER");
        userRole.setId(1L);
    }

    @Test
    void shouldSaveUserCorrectly() {
        // Arrange
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        userService.saveUser(userDto);

        // Assert
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRoles()).containsExactly(userRole);
    }
    
    @Test
    void shouldSaveUserAndCreateRoleIfNotExists() {
        // Arrange
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null); // Role doesn't exist
        when(roleRepository.save(any(Role.class))).thenReturn(userRole); // Mock saving the new role
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

         // Act
        userService.saveUser(userDto);

         // Assert
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(roleRepository, times(1)).save(roleCaptor.capture()); // Verify role was saved
        assertThat(roleCaptor.getValue().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldFindByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("found@example.com");
        when(userRepository.findByEmail("found@example.com")).thenReturn(user);

        // Act
        User foundUser = userService.findByEmail("found@example.com");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("found@example.com");
    }
}