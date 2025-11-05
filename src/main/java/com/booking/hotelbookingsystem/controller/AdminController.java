package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.dto.HotelDto;
import com.booking.hotelbookingsystem.dto.RoomDto;
import com.booking.hotelbookingsystem.model.Hotel;
// --- REPOSITORIES ARE REMOVED FROM IMPORTS ---
import com.booking.hotelbookingsystem.service.*; // Import all services
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // --- DEPENDENCIES ARE NOW ONLY SERVICES ---
    private final HotelService hotelService;
    private final RoomService roomService;
    private final UserService userService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    // --- UPDATED CONSTRUCTOR ---
    public AdminController(HotelService hotelService, RoomService roomService,
                           UserService userService, BookingService bookingService,
                           PaymentService paymentService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    // --- UPDATED DASHBOARD METHOD ---
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // Fetch real counts from the services
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalHotels", hotelService.countHotels());
        model.addAttribute("totalBookings", bookingService.countBookings());
        model.addAttribute("totalSuccessfulPayments", paymentService.countSuccessfulPayments());
        
        return "admin/dashboard";
    }

    // --- Hotel CRUD (No Changes) ---
    @GetMapping("/hotels")
    public String showHotelList(Model model) {
        model.addAttribute("hotels", hotelService.findAllHotels());
        return "admin/hotels-list";
    }

    @GetMapping("/hotels/add")
    public String showAddHotelForm(Model model) {
        model.addAttribute("hotel", new HotelDto());
        return "admin/hotels-add";
    }

    @PostMapping("/hotels/add")
    public String saveHotel(@Valid @ModelAttribute("hotel") HotelDto hotelDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/hotels-add";
        }
        hotelService.saveHotel(hotelDto);
        redirectAttributes.addFlashAttribute("successMessage", "Hotel added successfully!");
        return "redirect:/admin/hotels";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            HotelDto hotelDto = hotelService.findHotelDtoById(id);
            model.addAttribute("hotel", hotelDto);
            return "admin/hotels-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hotel not found.");
            return "redirect:/admin/hotels";
        }
    }

    @PostMapping("/hotels/edit/{id}")
    public String updateHotel(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("hotel") HotelDto hotelDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/hotels-edit";
        }
        hotelDto.setId(id);
        hotelService.updateHotel(hotelDto);
        redirectAttributes.addFlashAttribute("successMessage", "Hotel updated successfully!");
        return "redirect:/admin/hotels";
    }

    @PostMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
         try {
            hotelService.deleteHotel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hotel deleted successfully!");
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Could not delete hotel. It might have existing bookings.");
        }
        return "redirect:/admin/hotels";
    }

    
    // --- Room CRUD (No Changes) ---
    @GetMapping("/hotels/{hotelId}/rooms")
    public String showHotelRooms(@PathVariable("hotelId") Long hotelId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = hotelService.findHotelById(hotelId);
            model.addAttribute("hotel", hotel);
            model.addAttribute("rooms", roomService.findRoomsByHotelId(hotelId));
            return "admin/rooms-list";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hotels";
        }
    }

    @GetMapping("/hotels/{hotelId}/rooms/add")
    public String showAddRoomForm(@PathVariable("hotelId") Long hotelId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = hotelService.findHotelById(hotelId);
            RoomDto roomDto = new RoomDto();
            roomDto.setHotelId(hotelId);
            model.addAttribute("hotelName", hotel.getName());
            model.addAttribute("room", roomDto);
            return "admin/rooms-add";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hotels";
        }
    }
    
    @PostMapping("/hotels/{hotelId}/rooms/add")
    public String saveRoom(@PathVariable("hotelId") Long hotelId,
                           @Valid @ModelAttribute("room") RoomDto roomDto,
                           BindingResult bindingResult,
                           Model model, RedirectAttributes redirectAttributes) {
        
        roomDto.setHotelId(hotelId);
        Hotel hotel;
        try {
            hotel = hotelService.findHotelById(hotelId);
        } catch (EntityNotFoundException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Hotel not found.");
             return "redirect:/admin/hotels";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("hotelName", hotel.getName());
            return "admin/rooms-add";
        }
        try {
            roomService.saveRoom(roomDto);
             redirectAttributes.addFlashAttribute("successMessage", "Room added successfully!");
        } catch (IllegalArgumentException e) {
             model.addAttribute("hotelName", hotel.getName());
             bindingResult.reject("globalError", e.getMessage());
             return "admin/rooms-add";
        }
        return "redirect:/admin/hotels/" + hotelId + "/rooms";
    }

    @GetMapping("/rooms/edit/{roomId}")
    public String showEditRoomForm(@PathVariable("roomId") Long roomId, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoomDto roomDto = roomService.findRoomDtoById(roomId);
            Hotel hotel = hotelService.findHotelById(roomDto.getHotelId());
            
            model.addAttribute("room", roomDto);
            model.addAttribute("hotelName", hotel.getName());
            return "admin/rooms-edit";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hotels";
        }
    }

    @PostMapping("/rooms/edit/{roomId}")
    public String updateRoom(@PathVariable("roomId") Long roomId,
                             @Valid @ModelAttribute("room") RoomDto roomDto,
                             BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        
        roomDto.setId(roomId);
        
        if (bindingResult.hasErrors()) {
            try {
                Hotel hotel = hotelService.findHotelById(roomDto.getHotelId());
                model.addAttribute("hotelName", hotel.getName());
            } catch (EntityNotFoundException e) {
                // Handle case where hotel ID is missing/invalid during an error
            }
            return "admin/rooms-edit";
        }

        roomService.updateRoom(roomDto);
        redirectAttributes.addFlashAttribute("successMessage", "Room updated successfully!");
        return "redirect:/admin/hotels/" + roomDto.getHotelId() + "/rooms";
    }

    @PostMapping("/rooms/delete/{roomId}")
    public String deleteRoom(@PathVariable("roomId") Long roomId, RedirectAttributes redirectAttributes) {
        Long hotelId = null;
        try {
            RoomDto roomDto = roomService.findRoomDtoById(roomId);
            hotelId = roomDto.getHotelId();
            
            roomService.deleteRoom(roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete room. It might have existing bookings.");
        }
        
        if (hotelId != null) {
            return "redirect:/admin/hotels/" + hotelId + "/rooms";
        } else {
            return "redirect:/admin/hotels";
        }
    }
}