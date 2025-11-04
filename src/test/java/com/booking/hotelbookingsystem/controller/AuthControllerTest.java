package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.config.SecurityConfig;
import com.booking.hotelbookingsystem.dto.UserDto;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("deprecation") // Suppress warnings for @MockBean
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService; // Mock security dependency

    private UserDto userDto;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(null, "Test User", "test@example.com", "password123");
        existingUser = new User();
        existingUser.setEmail("existing@example.com");
    }

    @Test
    @WithAnonymousUser
    void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithAnonymousUser
    void shouldRegisterUserSuccessfully() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(null);
        doNothing().when(userService).saveUser(any(UserDto.class));

        mockMvc.perform(post("/register/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", userDto.getName())
                        .param("email", userDto.getEmail())
                        .param("password", userDto.getPassword())
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register?success"));
    }

    @Test
    @WithAnonymousUser
    void shouldFailRegistrationWhenEmailExists() throws Exception {
        when(userService.findByEmail(userDto.getEmail())).thenReturn(existingUser);

        mockMvc.perform(post("/register/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", userDto.getName())
                        .param("email", userDto.getEmail())
                        .param("password", userDto.getPassword())
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    @WithAnonymousUser
    void shouldShowLoginPage() throws Exception {
         mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}