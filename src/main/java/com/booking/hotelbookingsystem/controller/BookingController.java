package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.dto.BookingDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.RoomRepository;
import com.booking.hotelbookingsystem.service.BookingService;
import com.booking.hotelbookingsystem.service.PaymentService;
import com.booking.hotelbookingsystem.service.UserService;

// --- ADD THIS IMPORT ---
import jakarta.persistence.EntityNotFoundException;
// --- END IMPORT ---

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final RoomRepository roomRepository;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService, UserService userService,
                             RoomRepository roomRepository, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.roomRepository = roomRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/book/{roomId}")
    public String showBookingConfirmation(
            @PathVariable("roomId") Long roomId,
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            Model model, RedirectAttributes redirectAttributes) {

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Room selected.");
            return "redirect:/";
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) numberOfNights = 1; 
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        model.addAttribute("room", room);
        model.addAttribute("hotel", room.getHotel());
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("numberOfNights", numberOfNights);
        model.addAttribute("totalPrice", totalPrice);

        return "booking-confirm";
    }

    @PostMapping("/book/confirm")
    public String processBooking(
            @RequestParam("roomId") Long roomId,
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        try {
            BookingDto createdBooking = bookingService.createBooking(roomId, user, checkInDate, checkOutDate);
            return "redirect:/payment/" + createdBooking.getId();

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            String searchUrl = "/"; 
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null) {
                 searchUrl = String.format("/search?destination=%s&checkin=%s&checkout=%s",
                                 room.getHotel().getCity(), checkInDate, checkOutDate);
            }
            return "redirect:" + searchUrl;
        }
    }

    @GetMapping("/payment/{bookingId}")
    public String showPaymentPage(@PathVariable("bookingId") Long bookingId, Model model, RedirectAttributes redirectAttributes) {
         try {
             Booking booking = bookingService.findBookingById(bookingId);
             
             long numberOfNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
             if (numberOfNights <= 0) numberOfNights = 1;
             BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

             model.addAttribute("booking", booking);
             model.addAttribute("totalPrice", totalPrice);

             return "payment-page";

         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Invalid booking for payment.");
             return "redirect:/my-bookings";
         }
    }

    @PostMapping("/payment/process")
    public String processPayment(
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {

         Booking booking = bookingService.findBookingById(bookingId);
         if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/my-bookings";
         }

         try {
            Payment payment = paymentService.processPayment(booking, amount); 

            if ("SUCCESS".equals(payment.getPaymentStatus())) {
                 redirectAttributes.addFlashAttribute("successMessage", "Payment successful! Your booking is confirmed.");
            } else {
                 redirectAttributes.addFlashAttribute("errorMessage", "Payment failed. Please try again.");
            }
            return "redirect:/my-bookings";

         } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing payment: " + e.getMessage());
            return "redirect:/payment/" + bookingId;
         }
    }

     @GetMapping("/my-bookings")
     public String showMyBookings(Model model) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         User user = userService.findByEmail(authentication.getName());

         if (user == null) {
             return "redirect:/login"; 
         }

         List<BookingDto> bookings = bookingService.findBookingsByUserId(user.getId());
         model.addAttribute("bookings", bookings);

        return "my-bookings";
     }

    // --- METHOD WITH THE ERROR ---
    @PostMapping("/booking/cancel/{bookingId}")
    public String cancelBooking(@PathVariable("bookingId") Long bookingId,
                                RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
             return "redirect:/login";
        }

        // --- FIX IS HERE: Catch the specific exceptions ---
        try {
            bookingService.cancelBooking(bookingId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " has been cancelled.");
        } catch (EntityNotFoundException | IllegalStateException e) { // Use imported EntityNotFoundException
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // --- END FIX ---

        return "redirect:/my-bookings";
    }
}