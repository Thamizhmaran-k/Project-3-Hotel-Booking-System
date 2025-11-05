package com.booking.hotelbookingsystem.controller;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.service.BookingService;
import com.booking.hotelbookingsystem.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Controller
public class PaymentController {

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    // --- THIS IS THE FIX ---
    @Value("${app.base-url}") // Injects the URL from application.properties
    private String baseUrl;
    // --- END FIX ---

    private final BookingService bookingService;
    private final PaymentService paymentService;

    public PaymentController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @GetMapping("/payment/{bookingId}")
    public String showPaymentPage(@PathVariable("bookingId") Long bookingId, Model model) {
        Booking booking = bookingService.findBookingById(bookingId);
        long numberOfNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (numberOfNights <= 0) numberOfNights = 1;
        BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        model.addAttribute("booking", booking);
        model.addAttribute("totalPrice", totalPrice);
        return "payment-page";
    }

    @PostMapping("/payment/create-checkout-session")
    public String createCheckoutSession(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        
        Booking booking = bookingService.findBookingById(bookingId);
        
        long numberOfNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (numberOfNights <= 0) numberOfNights = 1;
        BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));
        
        long amountInCents = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();

        // The baseUrl variable is now dynamic from your properties file
        
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // Use the injected baseUrl variable here
                .setSuccessUrl(baseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}&booking_id=" + bookingId)
                .setCancelUrl(baseUrl + "/payment/cancel?booking_id=" + bookingId)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd") 
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Booking for: " + booking.getRoom().getHotel().getName())
                                        .setDescription(booking.getRoom().getRoomType() + " from " + booking.getCheckInDate())
                                        .build()
                                ).build()
                        ).build()
                ).build();

            Session session = Session.create(params);
            
            return "redirect:" + session.getUrl();

        } catch (StripeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Could not connect to Stripe. Check your API Key.");
            return "redirect:/payment/" + bookingId;
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("session_id") String sessionId,
                                 @RequestParam("booking_id") Long bookingId,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            Session session = Session.retrieve(sessionId);
            
            if ("paid".equals(session.getPaymentStatus())) {
                Booking booking = bookingService.findBookingById(bookingId);
                BigDecimal amountPaid = BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100));

                paymentService.processPayment(booking, amountPaid);

                redirectAttributes.addFlashAttribute("successMessage", "Payment successful! Your booking is confirmed.");
            } else {
                 redirectAttributes.addFlashAttribute("errorMessage", "Payment was not successful.");
            }
            return "redirect:/my-bookings";

        } catch (StripeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Payment verification failed.");
            return "redirect:/my-bookings";
        }
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(@RequestParam("booking_id") Long bookingId,
                                RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("errorMessage", "Payment was cancelled. Your booking is not confirmed.");
        return "redirect:/my-bookings";
    }
}