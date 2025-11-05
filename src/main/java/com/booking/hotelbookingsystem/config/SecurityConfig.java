package com.booking.hotelbookingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(
                                        "/",
                                        "/register/**",
                                        "/login/**",
                                        "/search/**", // Search is public
                                        "/css/**",
                                        "/js/**",
                                        "/images/**" 
                                ).permitAll()
                                
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                
                                .requestMatchers(
                                        "/profile/**",
                                        "/my-bookings/**",
                                        "/book/**",     // Booking requires login
                                        "/payment/**"   // Payment requires login
                                ).authenticated()
                                
                                .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/login") 
                                .loginProcessingUrl("/login") 
                                // *** THIS IS THE FIX ***
                                // We remove 'true' so it redirects to the saved page
                                .defaultSuccessUrl("/profile") 
                                .permitAll() 
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll() 
                );
        
        return http.build();
    }
}