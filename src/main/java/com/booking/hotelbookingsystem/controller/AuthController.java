package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.dto.UserDto;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Handler for the home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Handler for the login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Handler to show the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Create a new UserDto to hold form data
        model.addAttribute("user", new UserDto());
        return "register";
    }

    // Handler to process the registration form
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult bindingResult,
                               Model model) {
        
        // Check if the user (email) already exists
        User existingUser = userService.findByEmail(userDto.getEmail());
        if (existingUser != null) {
            bindingResult.rejectValue("email", "email.exists", 
                                  "An account with this email already exists.");
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDto); // Send the user back to the form
            return "register";
        }

        // If no errors, save the user
        userService.saveUser(userDto);
        return "redirect:/register?success";
    }

    // Handler for the user profile page (after successful login)
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}