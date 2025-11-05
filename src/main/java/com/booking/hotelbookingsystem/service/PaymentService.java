package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;
import java.math.BigDecimal;

public interface PaymentService {
    Payment processPayment(Booking booking, BigDecimal amount);
    
    // --- ADD THIS METHOD ---
    long countSuccessfulPayments();
}