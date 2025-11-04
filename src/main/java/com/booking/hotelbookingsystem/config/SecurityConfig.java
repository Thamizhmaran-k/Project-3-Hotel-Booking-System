package com.booking.hotelbookingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// We no longer need AntPathRequestMatcher
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher; 

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
                .csrf(csrf -> csrf.disable()) // CSRF should be enabled in production
                
                // Define authorization rules for different paths
                .authorizeHttpRequests((authorize) ->
                        authorize
                                // --- MODERN FIX: Use direct path strings ---
                                .requestMatchers(
                                        "/",
                                        "/register/**",
                                        "/login/**",
                                        "/search/**",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**" // Allow images folder
                                ).permitAll()
                                
                                // Admin-only paths
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                
                                // Paths for any authenticated user (USER or ADMIN)
                                .requestMatchers(
                                        "/profile/**",
                                        "/my-bookings/**",
                                        "/book/**",
                                        "/payment/**"
                                ).authenticated()
                                // --- END MODERN FIX ---
                                
                                // All other requests must be authenticated
                                .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/login") 
                                .loginProcessingUrl("/login") 
                                .defaultSuccessUrl("/profile", true) 
                                .permitAll() 
                )
                // --- MODERN LOGOUT FIX ---
                .logout(
                        logout -> logout
                                .logoutUrl("/logout") // Specify the URL to trigger logout
                                .logoutSuccessUrl("/login?logout") // Redirect after logout
                                .permitAll() 
                );
                // --- END FIX ---
        
        return http.build();
    }
}