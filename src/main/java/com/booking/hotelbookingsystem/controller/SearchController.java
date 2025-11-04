package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.dto.HotelSearchResultDto;
import com.booking.hotelbookingsystem.service.HotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SearchController {

    private final HotelService hotelService;

    public SearchController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/search")
    public String searchHotels(
            @RequestParam("destination") String city,
            @RequestParam("checkin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkout") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Basic validation
        if (city.isEmpty() || checkInDate == null || checkOutDate == null || checkOutDate.isBefore(checkInDate.plusDays(1))) {
             redirectAttributes.addFlashAttribute("searchError", "Please provide a valid city and a check-out date that is after the check-in date.");
             return "redirect:/"; // Redirect back to homepage
        }

        List<HotelSearchResultDto> results = hotelService.searchAvailableHotels(city, checkInDate, checkOutDate);

        model.addAttribute("city", city);
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("results", results);

        return "search-results"; // Name of the new results HTML page
    }
}