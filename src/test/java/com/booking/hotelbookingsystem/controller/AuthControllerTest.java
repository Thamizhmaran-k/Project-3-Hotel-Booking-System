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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("deprecation")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

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
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("user"));
    }

    @Test
    @WithAnonymousUser
    void shouldRegisterUserSuccessfully() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(null);
        doNothing().when(userService).saveUser(any(UserDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/register/save")
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("name", userDto.getName())
                        .param("email", userDto.getEmail())
                        .param("password", userDto.getPassword())
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/register?success"));
    }

    @Test
    @WithAnonymousUser
    void shouldFailRegistrationWhenEmailExists() throws Exception {
        when(userService.findByEmail(userDto.getEmail())).thenReturn(existingUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/save")
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("name", userDto.getName())
                        .param("email", userDto.getEmail())
                        .param("password", userDto.getPassword())
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("user", "email"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("user"));
    }

    @Test
    @WithAnonymousUser
    void shouldShowLoginPage() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"));
    }
}