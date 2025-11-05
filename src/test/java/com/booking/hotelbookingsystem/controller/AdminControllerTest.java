package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.config.SecurityConfig;
import com.booking.hotelbookingsystem.dto.HotelDto;
// Import Services
import com.booking.hotelbookingsystem.service.BookingService;
import com.booking.hotelbookingsystem.service.HotelService;
import com.booking.hotelbookingsystem.service.PaymentService;
import com.booking.hotelbookingsystem.service.RoomService;
import com.booking.hotelbookingsystem.service.UserService;
// We no longer need to import Repositories
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("deprecation") // Suppress warnings for @MockBean
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- MOCKS ARE NOW SERVICES ---
    @MockBean private HotelService hotelService;
    @MockBean private RoomService roomService;
    @MockBean private UserService userService;
    @MockBean private BookingService bookingService;
    @MockBean private PaymentService paymentService;
    
    @MockBean private UserDetailsService userDetailsService; // Mock security dependency

    private HotelDto hotelDto;

    @BeforeEach
    void setUp() {
        hotelDto = new HotelDto();
        hotelDto.setId(1L);
        hotelDto.setName("Test Hotel");
        hotelDto.setCity("Test City");
        hotelDto.setAddress("123 Test St");
    }

    // --- NEW TEST for the Dashboard ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAdminDashboard() throws Exception {
        // Mock the new service calls
        when(userService.countUsers()).thenReturn(5L);
        when(hotelService.countHotels()).thenReturn(2L);
        when(bookingService.countBookings()).thenReturn(10L);
        when(paymentService.countSuccessfulPayments()).thenReturn(8L);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalUsers", 5L))
                .andExpect(model().attribute("totalHotels", 2L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowHotelListForAdmin() throws Exception {
        when(hotelService.findAllHotels()).thenReturn(Collections.singletonList(hotelDto));

        mockMvc.perform(get("/admin/hotels"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotels-list"))
                .andExpect(model().attributeExists("hotels"));
    }

     @Test
     @WithMockUser(roles = "USER")
     void shouldDenyAccessToHotelListForUser() throws Exception {
         mockMvc.perform(get("/admin/hotels"))
                .andExpect(status().isForbidden());
     }

     @Test
     @WithMockUser(roles = "ADMIN")
     void shouldShowAddHotelFormForAdmin() throws Exception {
          mockMvc.perform(get("/admin/hotels/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotels-add"))
                .andExpect(model().attributeExists("hotel"));
     }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveHotelSuccessfully() throws Exception {
        doNothing().when(hotelService).saveHotel(any(HotelDto.class));

        mockMvc.perform(post("/admin/hotels/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", hotelDto.getName())
                        .param("city", hotelDto.getCity())
                        .param("address", hotelDto.getAddress())
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailSaveHotelWhenNameIsEmpty() throws Exception {
        mockMvc.perform(post("/admin/hotels/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("city", hotelDto.getCity())
                        .param("address", hotelDto.getAddress())
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotels-add"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attributeHasFieldErrors("hotel", "name"));
    }
}