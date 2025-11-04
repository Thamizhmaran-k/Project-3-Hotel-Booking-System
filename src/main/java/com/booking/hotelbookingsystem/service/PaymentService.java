package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;

import java.math.BigDecimal;

public interface PaymentService {

    /**
     * Simulates processing a payment for a given booking and amount.
     * Records the payment details in the database.
     * @param booking The booking to associate the payment with.
     * @param amount The total amount to be "paid".
     * @return The created Payment object.
     */
    Payment processPayment(Booking booking, BigDecimal amount);
}